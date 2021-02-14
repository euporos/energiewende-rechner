(ns stromrechner.helpers)

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









