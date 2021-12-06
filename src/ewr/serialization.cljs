(ns ewr.serialization
  (:require
   [re-frame.core :as rf :refer [reg-event-db reg-sub]]
   ["huffman-url-compressor" :as huff :refer [createEncoder encodeConfig decodeConfig]] [wrap.compress :as compress]
   [clojure.edn :as edn]
   [malli.core :as m]
   [deercreeklabs.lancaster :as l]
   [clojure.string :as str]
   ["base64-arraybuffer" :as b64]))

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
;; ####### Float-compression #######
;; #################################

(def alphabet
  "abcdefghijklmn")

(defn compress-floatstrings
  [instring]
  (str/replace
   instring
   #"([0-9])\1{2,}"
   (fn [[m1 m2]]
     (str (get alphabet (count m1)) m2))))

(defn expand-floatstrings
  [compressed-string]
  (str/replace
   compressed-string
   #"([a-z])([0-9])"
   (fn [[full letter number]]
     (apply str
            (take (.indexOf alphabet letter)  (repeat number))))))

(expand-floatstrings
 (compress-floatstrings "8.600000000000001"))

;; ######################################
;; ######## Huffmann-Compression ########
;; ######################################

(def code-savestate-huff
  (let [encoder (createEncoder
                 (str alphabet "[1300 [[28 4.56 0.12 11 10260 240] [12 5.2 0.44 44 16447 142] [15 240.8 0.08 12 930] [2 0.16 4.63 230 1080] [12 482.1 2.82 490 572] [31 135.1 28.67 820 1185]]]"))]
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

;; ############################
;; ###### Main Functions ######
;; ############################

(defn decompress-and-deserialize
  "work around bug in Huffman-Library
  see https://stackoverflow.com/questions/67273883/information-lost-in-huffman-encoding"
  [savestate-string]
  (let [decoded (expand-floatstrings
                 (decode-savestate-huff savestate-string))
        parsed  (try (deserialize
                      (edn/read-string (str decoded "]")))
                     (catch js/Object e
                       (js/console.log "Error reading savestate… not loading")
                       nil))]

    (if (and parsed (m/validate savestate-spec parsed))
      parsed)))

(def serialize-and-compress
  (comp
   encode-savestate-huff
   compress-floatstrings
   str
   serialize))


;; ############
;; ### Avro ###
;; ############

(l/def-record-schema nrg-schema
  [:share l/float-schema]
  [:power-density l/float-schema]
  [:deaths l/float-schema]
  [:co2 l/float-schema]
  [:resources l/float-schema]
  [:arealess-capacity l/float-schema])

(l/def-record-schema nrg-sources-schema
  [:wind nrg-schema]
  [:solar nrg-schema]
  [:nuclear nrg-schema]
  [:bio nrg-schema]
  [:natural-gas nrg-schema]
  [:coal nrg-schema])

(l/def-record-schema savestate-schema
  [:energy-sources nrg-sources-schema]
  [:energy-needed l/float-schema])

(count
 (b64/encode
  (l/serialize savestate-schema
               {:energy-sources
                {:wind
                 {:share             28
                  :power-density     4.56
                  :deaths            0.12
                  :co2               11
                  :resources         10260
                  :arealess-capacity 240}
                 :solar
                 {:share             12
                  :power-density     5.2
                  :deaths            0.44
                  :co2               44
                  :resources         16447
                  :arealess-capacity 142}
                 :nuclear
                 {:share         15
                  :power-density 240.8
                  :deaths        0.08
                  :co2           12
                  :resources     930}
                 :bio
                 {:share         2
                  :power-density 0.16
                  :deaths        4.63
                  :co2           230
                  :resources     1080}
                 :natural-gas
                 {:share         12
                  :power-density 482.1
                  :deaths        2.82
                  :co2           490
                  :resources     572}
                 :coal
                 {:share         31
                  :power-density 135.1
                  :deaths        28.67
                  :co2           820
                  :resources     1185}}
                :energy-needed 1300})))
