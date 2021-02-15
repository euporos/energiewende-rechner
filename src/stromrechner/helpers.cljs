(ns stromrechner.helpers
  (:require
   [clojure.string :as str]))

(defn map-vals
  ""
  [f coll]
  (reduce
   (fn [sofar [key val]]
     (assoc sofar key (f val)))
   {} coll))

(defn reverse-paths
  ""
  [indata]
  (let [first-level-keys (keys indata)
        second-level-keys (keys (reduce merge (map second indata)))
        paths (for [flk first-level-keys
                    slk second-level-keys]
                [flk slk])]
    
    (reduce
     (fn [sofar nextpath]
       (assoc-in sofar (vec (reverse nextpath))
                 (get-in indata nextpath)))
     {} paths)))

(defn nan->nil
  ""
  [val]
  (if (js/isNaN val) nil val))

(defn nan->0
  ""
  [val]
  (if (js/isNaN val) 0 val))




(defn structure-int
  "Structures large integers
  by interposing it with whitespace"
  [integer]
  (if (= 0 integer)
    "0"
   (str/replace 
    (->> integer
         str
         reverse
         (partition 3 3 (repeat "0"))
         (interpose " ")
         flatten
         reverse
         (apply str)) #"^0*" "")))

