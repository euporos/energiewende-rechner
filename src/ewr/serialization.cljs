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
                 "[1300 [[10.7n97 4.56 0.12 11 10260 240] [46 5.2 0.44 44 16447 142] [10.7n97 240.8 0.08 12 930] [10.7n97 0.16 4.63 230 1080] [10.7n97 482.1 2.82 490 572] [10.7n97 135.1 28.67 820 1185]]]")]
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
