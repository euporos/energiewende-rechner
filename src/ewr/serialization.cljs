(ns ewr.serialization
  (:require
   ["huffman-url-compressor" :as huff :refer [createEncoder encodeConfig decodeConfig]]
   ["number-to-base64" :refer [ntob bton]]
   [clojure.data :as cd]
   [clojure.string :as str]
   [ewr.config :as cfg]))

(def test-state
  {:energy-sources
   {:wind
    {:share 604000
     ;; :cap 604000
     :power-density 4.56
     :deaths 0.12
     :co2 11
     :resources 10260
     :arealess-capacity 240}
    :solar
    {:share 259000
     :power-density 5.2
     :deaths 0.44
     :co2 44
     :resources 16447
     :arealess-capacity 142}
    :bio
    {:share 43000
     :power-density 0.16
     :deaths 4.63
     :co2 230
     :resources 1080}
    :nuclear
    {:share 324000
     :power-density 240.8
     :deaths 0.08
     :co2 12
     :resources 930}
    :natural-gas
    {:share 259000
     :power-density 482.1
     :deaths 2.82
     :co2 490
     :resources 572}
    :coal
    {:share 648000
     :power-density 135.1
     :deaths 28.67
     :co2 820
     :resources 1185}
    :hydro
    {:cap 42000
     :share 22000
     :power-density 2.28
     :deaths 0.14
     :co2 24
     :resources 14068}}
   :energy-needed 2159000})

;; ########################
;; ##### Delta-making ##### 
;; ########################

(defn deep-merge
  [& maps]
  (if (every? map? maps)
    (apply merge-with deep-merge maps)
    (last maps)))

(defn delta [savestate preset]
  (first (cd/diff savestate preset)))

;; ###########################
;; ###### Serialization ######
;; ###########################

(def level-1
  [:hydro
   :coal
   :natural-gas
   :nuclear
   :bio
   :solar
   :wind])

(def level-2
  ['(:share :int)
   '(:power-density :float)
   '(:deaths :float)
   '(:co2 :float)
   '(:resources :int)
   '(:arealess-capacity :int)
   '(:cap :int)])

(def nesteds
  (for [l-1-item level-1
        l-2-item level-2]
    (vec (conj l-2-item l-1-item :energy-sources))))

(def globals
  [[:energy-needed :int]
   nil
   nil
   nil
   nil])

(def codenda
  (into globals nesteds))

(defn codendum->char [codendum]
  (ntob (.indexOf codenda codendum)))

(defn char->codendum [char]
  (nth codenda (bton char)))

(defn serialize [savestate]
  (keep-indexed
   (fn [i codendum]
     (when codendum
       (let [path (pop codendum)
             type (peek codendum)
             value (get-in savestate path)]
         (when value
           (str (ntob i)
                (case type
                  :int (ntob value)
                  :float value))))))
   codenda))

(defn deserialize [serialized-savestate]
  (reduce
   (fn [result next]
     (let [[index-char & stringified] next
           stringified (apply str stringified)
           index (bton index-char)
           codendum (nth codenda index)
           path (pop codendum)
           type (peek codendum)]
       (assoc-in result  path
                 (case type
                   :int (bton stringified)
                   :float (js/parseFloat stringified)))))
   {}
   serialized-savestate))

;; #######################
;; ##### Compression #####
;; #######################

(def alphabet
  (apply str "          " "1234567890." (map ntob (range 64))))

(def encoder
  (createEncoder alphabet))

(defn huff-encode [string]
  ;; padding is needed so base64 will work
  (let [needed-padding (- 3 (mod (count string) 3))
        padding (apply str (repeat needed-padding " "))]
    (when string (encodeConfig (str string padding) encoder))))

(defn huff-decode [string]
  (decodeConfig string encoder))

(defn encode  [savestate]
  (huff-encode
   (str/join " " (serialize savestate))))

(defn decode [encoded-savestate]
  (deserialize (str/split
                (huff-decode encoded-savestate) " ")))

(defn savestate->string [savestate]
  (let [preset cfg/latest-preset
        preset-index (dec (count cfg/presets))
        delta (delta  savestate preset)]
    (when delta
      (str preset-index "~" (encode delta)))))

(defn string->savestate [encoded]
  (if (seq encoded)
    (let [[preset-index encoded-delta] (str/split encoded "~")
          preset-index (js/parseInt preset-index)
          preset (nth cfg/presets preset-index)
          decoded-delta (decode encoded-delta)]
      (deep-merge preset decoded-delta))
    cfg/latest-preset))


