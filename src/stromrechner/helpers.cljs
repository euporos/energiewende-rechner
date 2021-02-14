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




(reverse-paths
 (reverse-paths
  {:flaechenverbrauch {:biogas 120, :wind 40, :solar 140, :kern 0.1},
   :vollast {:biogas 0.8, :solar 0.33, :wind 0.45, :kern 0.85},
   :tote {:biogas 2, :solar 0.4, :wind 0.1, :kern 0.9}}))




