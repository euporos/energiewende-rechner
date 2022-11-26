(ns ewr.csv
  (:require [clojure.string :as str]
            [ewr.config :as cfg]
            [ewr.remix :as remix]))

(def param-order [:share :power-density :deaths :co2 :resources :arealess-capacity :cap])

(defn savestate-to-csv
  ""
  [savestate]
  (str
   ":energy-needed," (remix/sum-shares (get savestate :energy-sources)) "\n"
   "energy-source\\Parameter," (str/join "," param-order) "\n"
   (str/join "\n"
             (map
              (fn [nrg-key]
                (str nrg-key ","
                     (str/join ","
                               (map
                                (fn [param-key]
                                  (get-in savestate
                                          [:energy-sources nrg-key param-key]))
                                param-order))))
              cfg/nrg-keys))))

