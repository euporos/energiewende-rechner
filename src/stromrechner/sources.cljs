(ns stromrechner.sources
  (:require [stromrechner.helpers :as h]
            [stromrechner.config :as cfg])
  (:require-macros [stromrechner.macros :as m]))



(defn transpose-energy-sources
  "The publication file is more comfortably
  edited by "
  [publications]
  (map
   (fn [pub]
     (if (:energy-sources pub) 
       (update pub :energy-sources
               #(h/reverse-paths %))
       pub))
   publications))


(def publications (transpose-energy-sources
                   (:publications cfg/config)))

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

;; #################
;; #### Helpers ####
;; #################

;; these come in handy when converting
;; values from the
;; to units used in the publications.edn
;; thes are not used in the program itself

(defn annual-twh-per-km2-to-W-per-m2
  ""
  [input]  
  (-> (/ 1 input) ; km² / TWh → TWh/km²
      (/ 1000000) ; TWh/m²
      (* 1000000000000) ; Wh/m² (per year)
      (/ (* 24 365)) ; W/m²
      ))

(comment
  (h/map-vals
  #(-> %
       annual-twh-per-km2-to-W-per-m2
       (* 100)
       Math/round
       (/ 100.0))
  {:solar 5.7
   :wind 46    
   :bio 95 
   :nuclear 0.1
   :natural-gas 1.1
   :coal 2.2}))
 

