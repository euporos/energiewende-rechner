(ns stromrechner.sources
  (:require [stromrechner.helpers :as h])
  (:require-macros [stromrechner.macros :as m])) 


(defn transpose-energy-sources
  ""
  [publications]
  (map
   (fn [pub]
     (if (:energy-sources pub)
       (update pub :energy-sources
               #(h/reverse-paths %))
       pub))
   publications))

 
 
(m/def-from-file publications
  "config/publications.edn" 
  transpose-energy-sources)   

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
 

