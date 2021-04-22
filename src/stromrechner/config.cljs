(ns stromrechner.config
  (:require [stromrechner.helpers :as h]
            [stromrechner.color :as color]
            [clojure.string :as str]
            [stromrechner.google-defines :as gf])
  (:require-macros [stromrechner.macros :as m]))

;; ##########################################
;; ######### Enrichment of Settings #########
;; ##########################################

;; some constants can be derived from the settings right away
;; to avoid recalculation

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


;; ####################################################################
;; ############## Load the Settings from the config file ##############
;; ####################################################################

(m/def-from-file settings
  "config/settings.edn"
  enrich-settings)
  
;; (m/def-config config gf/config-dir
;;   )
  
 

;; ##############################################################
;; ############# Extraction of Configuration Values #############
;; ##############################################################

(def nrgs (get settings :nrg-constants))

(def nrg-keys (map first (get settings :init-mix)))

(def total-landmass (:total-landmass settings))

(def co2-colors (:co2-colors settings))

(def snippet-directory (:snippet-directory settings))

(defn feature-active?
  ""
  [feature-key]
  ((:features settings) feature-key))

(defn icon-for-nrg
  ""
  [nrg-key]
  (get-in nrgs [nrg-key :icon]))
