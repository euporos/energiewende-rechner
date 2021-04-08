(ns stromrechner.config
  (:require [stromrechner.helpers :as h]
            [stromrechner.color :as color])
  (:require-macros [stromrechner.macros :as m]
                   ))

(def debug?
  ^boolean goog.DEBUG)


(defn enrich-nrg-constants
  ""
  [nrg-constants]
  (h/map-vals #(-> %
                   (assoc :transparent-color
                          (color/make-transparent (:color %)))
                   (assoc :darker-color
                          (color/set-brightness (:color %) 0.42))
                   (assoc-in [:props :fill] (color/make-transparent (:color %) 0.65)))              
              nrg-constants))

(defn enrich-settings
  ""
  [settings]
  (update settings :nrg-constants enrich-nrg-constants))


(m/def-from-file settings 
  "config/settings.edn"
  enrich-settings)

(def nrgs (get settings :nrg-constants))

(def nrg-keys (map first (get settings :init-mix)))


(defn icon-for-nrg
  ""
  [nrg-key]
  (get-in nrgs [nrg-key :icon]))

 
