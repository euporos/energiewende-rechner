(ns ewr.serialization
  (:require
   [re-frame.core :as rf :refer [reg-event-db reg-sub]]
   ["huffman-url-compressor" :as huff :refer [createEncoder encodeConfig decodeConfig]] [wrap.compress :as compress]
   [clojure.edn :as edn]
   [malli.core :as m]
   [clojure.string :as str]))

;; ######################################
;; ######## Huffmann-Compression ########
;; ######################################

(def code-savestate-huff
  (let [encoder (createEncoder
                 "[1300 [[57 4.56 0.12 11 10260 240] [8.600000000000001 5.2 0.44 44 16447 142] [8.600000000000001 240.8 0.08 12 930] [8.600000000000001 0.16 4.63 230 1080] [8.600000000000001 482.1 2.82 490 572] [8.600000000000001 135.1 28.67 820 1185]]]")]
    (fn [string decode?]
      (if decode?
        (decodeConfig string encoder)
        (encodeConfig string encoder)))))

(defn encode-savestate-huff
  ""
  [string]
  (code-savestate-huff string nil))

(defn decode-savestate-huff
  ""
  [string]
  (code-savestate-huff string true))

;; ######################################
;; ######## Manual Serialization ########
;; ######################################

(def serializer-version 1)

(def nrg-order [:wind :solar :nuclear :bio :natural-gas :coal])

(def param-order [:share :power-density :deaths :co2 :resources :arealess-capacity])

(defn serialize
  ""
  [savestate]
  (let [{:keys [energy-needed energy-sources]} savestate]
    [energy-needed
     (mapv
      (fn [nrg-key]
        (vec
         (keep
          (fn [param-key]
            (get-in energy-sources [nrg-key param-key]))
          param-order))) nrg-order)]))

(defn deserialize
  ""
  [[energy-needed nrgs]]
  {:energy-needed energy-needed
   :energy-sources
   (zipmap
    nrg-order
    (map
     (partial zipmap param-order)
     nrgs))})

;; #################################
;; ####### CSV-serialization #######
;; #################################

(defn savestate-to-csv
  ""
  [savestate]
  (str
   ":energy-needed," (get savestate :energy-needed) "\n"
   "energy-source\\Parameter," (str/join "," param-order) "\n"
   (str/join "\n"
             (map
              (fn [nrg-key]
                (str nrg-key ","
                     (str/join ","
                               (map
                                (fn [param-key]
                                  (get-in savestate
                                          [:energy-sources nrg-key param-key]))
                                param-order))))
              nrg-order))))

;; ###############################################
;; ########## Application to savestates ##########
;; ###############################################

(def energy-source-spec
  [:map
   [:share float?]
   [:power-density float?]
   [:deaths float?]
   [:co2 float?]
   [:resources float?]
   [:arealess-capacity {:optional true} float?]])

(def savestate-spec
  [:map
   [:energy-sources
    [:map
     [:wind
      energy-source-spec]
     [:solar
      energy-source-spec]
     [:nuclear
      energy-source-spec]
     [:bio
      energy-source-spec]
     [:natural-gas
      energy-source-spec]
     [:coal energy-source-spec]]]
   [:energy-needed float?]])

(defn deserialize-savestate-string
  "work around bug in Huffman-Library
  see https://stackoverflow.com/questions/67273883/information-lost-in-huffman-encoding"
  [savestate-string]
  (let [decoded (decode-savestate-huff savestate-string)
        parsed  (try (deserialize
                      (edn/read-string (str decoded "]")))
                     (catch js/Object e
                       (js/console.log "Error reading savestate… not loading")
                       nil))]

    (if (and parsed (m/validate savestate-spec parsed))
      parsed)))

(comment
  (def failing-savestate
    {:energy-sources {:wind {:share 56, :power-density 4.56, :deaths 0.12, :co2 11, :resources 10260, :arealess-capacity 240}, :solar {:share 44, :power-density 5.2, :deaths 0.44, :co2 44, :resources 16447, :arealess-capacity 142}, :nuclear {:share 0, :power-density 240.8, :deaths 0.08, :co2 12, :resources 930}, :bio {:share 0, :power-density 0.16, :deaths 4.63, :co2 230, :resources 1080}, :natural-gas {:share 0, :power-density 482.1, :deaths 2.82, :co2 490, :resources 572}, :coal {:share 0, :power-density 135.1, :deaths 28.67, :co2 820, :resources 1185}}, :energy-needed 1300})

  (-> failing-savestate
      serialize
      str
      ;;compress/compress-b64
      encode-savestate-huff
      ;; count
      ;; edn/read-string
      ;; decode
      ;; (= @(rf/subscribe [:save/savestate]))
      ;; decode-savestate-huff
      ;; edn/read-string
      ))
