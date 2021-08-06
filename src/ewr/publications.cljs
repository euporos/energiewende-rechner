(ns ewr.publications
  (:require [ewr.helpers :as h]
            [ewr.config :as cfg])
  (:require-macros [ewr.macros :as m]))

(defn transpose-energy-sources
  "The publication file is more comfortably
  edited by when the parameter-key
  preceding the energy key. Within program
  it is the other way round. Thus, for every
  publication, this function changes e.g.:
  {:deaths
   {:bio 4.63
    :nuclear 0.08
    :natural-gas 2.82
    :coal 28.67}}
  →
  {:bio
   {:deaths 4.63}
  :nuclear
   {:deaths 0.08}
  :natural-gas
   {:deaths 2.82}
  :coal
   {:deaths 28.67}}"
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

(defn pubs-for-needed-power
  "Returns all publications providing a value
  for :energy-needed"
  []
  (filter
   #(get % :energy-needed)
   publications))

(defn pubs-for-param
  "Returns all publications
  that provide a value for a given
  combination of energy-source and parameter"
  [nrg-key param-key]
  (filter
   #(get-in % [:energy-sources nrg-key param-key])
   publications))

(defn matching-pubs-for-path
  "Given a value returns all publications
  that contain this value under PATH"
  [path value]
  (filter
   #(= (get-in % path) value)
   publications))

(defn matching-pubs
  "Given a value for a certain combination of
  energy-source and parameter, returns
  all publications providing the same value"
  [nrg-key param-key value]
  (matching-pubs-for-path [:energy-sources nrg-key param-key] value))

(defn default-pub
  "Returns the first publication that provides the a value
  for a given combination of energy-source and parameter"
  [nrg-key param-key]
  (first (pubs-for-param nrg-key param-key)))

;; #################
;; #### Helpers ####
;; #################

;; these functions come in handy when converting
;; values from the
;; to units used in the publications.edn
;; They are not used in the program itself

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
