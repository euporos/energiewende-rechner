(ns ewr.serialization
  (:require
   ["huffman-url-compressor" :as huff :refer [createEncoder encodeConfig decodeConfig]] [wrap.compress :as compress]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [malli.core :as m]))

;; ######################################
;; ######## Manual Serialization ########
;; ######################################

(def serializer-version 1)

(def nrg-order [:wind :solar :nuclear :bio :natural-gas :coal :minors])

(def param-order [:share :power-density :deaths :co2 :resources :arealess-capacity :cap])

(defn serialize
  ""
  [savestate]
  (let [{:keys [energy-needed energy-sources]} savestate]
    [energy-needed
     (mapv
      (fn [nrg-key]
        (vec
         (map
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

(defn compress-nils [serialized-string]
  (str/replace serialized-string #"(nil ?)" "_"))

#_(defn expand-nils [decompressed-string]
    (str/replace decompressed-string #"_+" #(str/join " " (repeat (count %) "nil"))))

(defn expand-nils [decompressed-string]
  (str/replace decompressed-string #"_" "nil "))

;; ######################################
;; ######## Huffmann-Compression ########
;; ######################################

(def code-savestate-huff
  (let [encoder (createEncoder
                 (str alphabet "[2160 [[28 4.56 0.12 11 10260 240 _] [12 5.2 0.44 44 16447 142 _] [15 240.8 0.08 12 930 __] [2 0.16 4.63 230 1080 __] [12 482.1 2.82 490 572 __] [30 135.1 28.67 820 1185 __] [1 1 20 100 1d0 _ 500]]]"))]
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
   [:arealess-capacity {:optional true} [:maybe float?]]
   [:cap {:optional true} [:maybe float?]]])

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
     [:coal energy-source-spec]
     [:minors energy-source-spec]]]
   [:energy-needed float?]])

;; ############################
;; ###### Main Functions ######
;; ############################

(defn decompress-and-deserialize
  "work around bug in Huffman-Library
  see https://stackoverflow.com/questions/67273883/information-lost-in-huffman-encoding"
  [savestate-string]
  (let [decoded (-> savestate-string
                    decode-savestate-huff
                    expand-floatstrings
                    expand-nils)
        parsed  (try (deserialize
                      (edn/read-string (str decoded "]")))
                     (catch js/Object e
                       (js/console.log "Error reading savestate… not loading")
                       nil))]

    (if (and parsed (m/validate savestate-spec parsed))
      parsed
      (js/console.error  (m/explain savestate-spec parsed)))))

(def serialize-and-compress
  (comp
   encode-savestate-huff
   compress-floatstrings
   compress-nils
   str
   serialize))
