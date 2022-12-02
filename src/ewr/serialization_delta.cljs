(ns ewr.serialization-delta
  (:require
   ["huffman-url-compressor" :as huff :refer [createEncoder encodeConfig decodeConfig]]
   [clojure.data :as cd]
   [clojure.set :as cs]
   [ewr.config :as cfg]
   [ewr.serialization-common :as sercom]))

(defn deep-merge
  "Like merge, but merges maps recursively."
  {:added "1.7"}
  [& maps]
  (if (every? map? maps)
    (apply merge-with deep-merge maps)
    (last maps)))

(defn delta [savestate common-savestate]
  (first (cd/diff savestate common-savestate)))

(def mappings-l1
  {"m" :hydro
   "c" :coal
   "g" :natural-gas
   "n" :nuclear
   "b" :bio
   "s" :solar
   "w" :wind})

(def mappings-l1-r (cs/map-invert mappings-l1))

(def mappings-l2
  {""  :share
   "p" :power-density
   "d" :deaths
   "c" :co2
   "r" :resources
   "s" :arealess-capacity})

;; ######################################
;; ######## Huffmann-Compression ########
;; ######################################

(def encoder
  (createEncoder
   (str "9"
        (apply str (keys mappings-l1))
        (apply str (keys mappings-l2))
        "2160m0.680k54c20.41k6g8.1l68n10.208j3b1.36k107s8.1l68w51")))

(defn huff-encode [string]
  (when string (encodeConfig string encoder)))

(defn huff-decode [string]
  (decodeConfig string encoder))

;; #######################
;; ##### Compression #####
;; #######################

(def mappings-l2-r (cs/map-invert mappings-l2))

(def value-regex (str "[" sercom/alphabet  "0-9.]+"))

(def l1-regex (str "[" (apply str (keys mappings-l1)) "]"))

(def l2-regex (str "[" (apply str (keys mappings-l2)) "]?"))

(def param-regex (str "(" l1-regex ")"
                      "(" l2-regex ")"
                      "(" value-regex ")"))

(def param-regex-compiled
  (re-pattern (str "(" l1-regex ")"
                   "(" l2-regex ")"
                   "(" value-regex ")")))

(def encoded-regex-compiled
  (re-pattern
   (str "(^[0-9]*)"
        "((" param-regex ")*)")))

(defn encode-param [[param-key value]]
  (str (get mappings-l2-r param-key) (sercom/compress-floatstrings (str value))))

(defn encode-energy-source [[key params :as _nrg]]
  (let [key-char (get mappings-l1-r key)]
    (apply str (map #(str key-char (encode-param %)) params))))

(defn encode-energy-sources [nrgs]
  (apply str (map encode-energy-source nrgs)))

(defn encode-delta [{:keys [energy-needed energy-sources] :as savestate}]
  (when savestate
    (str energy-needed
         (when energy-sources (encode-energy-sources energy-sources)))))

(defn encode [savestate]
  (str (dec (count cfg/savestates))
       "~"
       (huff-encode
        (encode-delta
         (delta savestate (last cfg/savestates))))))

(defn decode-energy-source [sofar [_ l1 l2 value split-encoded-nrg]]
  (assoc-in sofar
            [(get mappings-l1 l1)
             (get mappings-l2 l2)]
            (js/parseFloat
             (sercom/expand-floatstrings value))))

(defn decode-energy-sources [encoded-nrgs]
  (reduce
   decode-energy-source
   {}
   (re-seq param-regex-compiled encoded-nrgs)))

(defn decode-delta [encoded-string]
  (when-let [[_ energy-needed nrgs] (re-find
                                     encoded-regex-compiled
                                     encoded-string)]
    (cond-> {}
      (seq energy-needed) (assoc :energy-needed (js/parseFloat energy-needed))
      nrgs (assoc :energy-sources (decode-energy-sources nrgs)))))

(defn decode [encoded-string]
  (when-let [[_ preset-n encoded-delta] (re-find #"^([0-9]+)~(.+$)" encoded-string)]
    (deep-merge
     (get cfg/savestates (js/parseInt preset-n))
     (decode-delta (huff-decode encoded-delta)))))
