(ns ewr.serialization-common
  (:require
   [clojure.string :as str]))

(def alphabet
  "zyxvutaefhijkl")

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
