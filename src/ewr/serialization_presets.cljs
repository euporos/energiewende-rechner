(ns ewr.serialization-presets
  (:require
   ["huffman-url-compressor" :as huff :refer [createEncoder encodeConfig decodeConfig]]
   [clojure.data :as cd]
   [clojure.set :as cs]
   [ewr.serialization-common :as sercom]))

(defn deep-merge
  "Like merge, but merges maps recursively."
  {:added "1.7"}
  [& maps]
  (if (every? map? maps)
    (apply merge-with deep-merge maps)
    (last maps)))

(def presets
  [{:energy-sources
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
     :bio
     {:share         2
      :power-density 0.16
      :deaths        4.63
      :co2           230
      :resources     1080}
     :nuclear
     {:share         15
      :power-density 240.8
      :deaths        0.08
      :co2           12
      :resources     930}
     :natural-gas
     {:share         12
      :power-density 482.1
      :deaths        2.82
      :co2           490
      :resources     572}
     :coal
     {:share         30
      :power-density 135.1
      :deaths        28.67
      :co2           820
      :resources     1185}
     :minors
     {:share         1
      :power-density 1
      :deaths        20
      :co2           100
      :resources     1000
      :cap           500}}
    :energy-needed 2159}])

(defn delta [savestate common-savestate]
  (first (cd/diff savestate common-savestate)))

(def mappings-l1
  {"m" :minors
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
  (encodeConfig string encoder))

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

(defn encode-delta [{:keys [energy-needed energy-sources] :as _savestate}]
  (str energy-needed
       (when energy-sources (encode-energy-sources energy-sources))))

(defn encode [savestate]
  (first
   (sort-by count
            (map-indexed
             (fn [n preset]
               (str n "~" (huff-encode
                           (encode-delta
                            (delta savestate preset)))))
             presets))))

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
     (get presets (js/parseInt preset-n))
     (decode-delta (huff-decode encoded-delta)))))
