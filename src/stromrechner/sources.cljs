(ns stromrechner.sources
  (:require-macros [stromrechner.macros :as m]))


(m/def-from-file publications 
  "resources/publications.edn")




(defn pubs-for-param
  ""
  [nrg-key param-key]
  (filter
   #(get-in % [:energy-sources nrg-key param-key])   
   publications))

(defn matching-pubs
  ""
  [nrg-key param-key value]
  (filter
   #(= (get-in % [:energy-sources nrg-key param-key]) value)
   publications))

(defn matching-pubs-for-path
  ""
  [path value]
  (filter
   #(= (get-in % path) value)
   publications))

(defn pubs-for-needed-power
  "Returns all publications providing a value
  for :needed Power"
  []
  (filter
   #(get % :energy-needed)   
   publications))



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


(defn default-pub
  ""
  [nrg-key param-key]
  (some #(if (get-in % [:energy-sources nrg-key param-key]) %)
        publications))




(reverse-paths
 {:flaechenverbrauch {:bio 120, :wind 40, :solar 140, :kern 0.1},
  :vollast {:bio 0.8, :solar 0.33, :wind 0.45, :kern 0.85},
  :deaths {:bio 2, :solar 0.4, :wind 0.1, :kern 0.9}})






  
 
