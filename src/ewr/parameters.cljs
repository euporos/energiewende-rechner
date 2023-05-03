(ns ewr.parameters
  (:require [ewr.config :as cfg]
            [ewr.constants :as const]
            [ewr.helpers :as h]))

;; ###########################
;; ###### Energy Needed ######
;; ###########################

(def energy-needed
  [:energy-needed {:name        "Strombedarf"
                   :unit        "TWh"
                   :granularity-factor const/granularity-factor
                   :parse-fn    js/parseFloat
                   :validation-fn #(and (int? %) (> % 0))
                   :input-attrs {:type    "number"
                                 :pattern "1"
                                 :step    "1"
                                 :min     0}}])

;; ##############################################
;; ########## Common Energy-parameters ##########
;; ##############################################

;; these Parameters must be defined for all parameters

(def common-nrg-parameters
  (map (fn [[param-key param-dfn]]
         ;; Overrides with the snippets
         ;; from the config file
         [param-key (merge
                     {:validation-fn (constantly true)}
                     param-dfn
                     (cfg/snippet :common-parameter-inputs param-key))])
       [[:power-density {:name        "Bemessungsleistung pro m² in W"
                         :unit        "W/m²granularity-factor b"
                         :parse-fn    js/parseFloat
                         :input-attrs {:type    "number"
                                       :pattern "0.00"
                                       :step    "0.01"
                                       :min     0.01}}]

        [:deaths {:name                "Todesfälle/TWh"
                  :unit                "/TWh"
                  :parse-fn            js/parseFloat
                  :indicator-formatter #(h/structure-int
                                         (Math/round %))
                  :input-attrs         {:type    "number"
                                        :pattern "0.00"
                                        :step    "0.01"
                                        :min     0.01}}]

        [:co2 {:name                [:span "Spezifische CO" [:sub "2"] "-Emiss. in g/kWh"]
               :unit                "g/kWh"
               :indicator-formatter #(-> %
                                         (* 0.001) ; convert to Mio t
                                         (* 10)
                                         Math/round
                                         (/ 10))
               :abs-unit            "Mio. t"
               :parse-fn            js/parseInt
               :input-attrs         {:type    "number"
                                     :pattern "0"
                                     :step    "1"
                                     :min     1}}]

        [:resources {:name                "Ressourcenverbrauch in t/TWh"
                     :unit                "t/TWh"
                     :abs-unit            "kt"
                     :parse-fn            js/parseFloat
                     :indicator-formatter #(-> %
                                               (* 0.001) ; convert to Mio t
                                               Math/round
                                               (h/structure-int))
                     :input-attrs         {:type    "number"
                                           :pattern "0.00"
                                           :step    "0.01"
                                           :min     0.01}}]]))

(def common-parameter-map (into {} common-nrg-parameters))

(def common-param-keys (map first common-nrg-parameters))

;; ###############################################
;; ########## Special Energy Parameters ##########
;; ###############################################

;; Parameters that follow the general pattern,
;; but are not defined for all Energy-sources
;; currently there's only one

(def arealess-capacity
  [:arealess-capacity {:unit        "TWh"
                       :granularity-factor const/granularity-factor
                       :parse-fn    js/parseFloat
                       :validation-fn (constantly true)
                       :input-attrs {:type    "number"
                                     :pattern "1"
                                     :step    "1"
                                     :min     0}}])

(def arealess-capacity-solar
  (assoc-in arealess-capacity
            [1 :name] (or
                       (cfg/snippet :common-parameter-inputs
                                    :arealess-capacity :name :solar)
                       "Solarkapazität auf Dächern in TWh")))

(def cap
  [:cap {:name                "Deckelung der Wasserkraft in TWh"
         :unit                "TWh"
         :validation-fn (constantly true)
         :granularity-factor const/granularity-factor
         :parse-fn            js/parseInt
         :input-attrs         {:type    "number"
                               :pattern "0"
                               :step    "1"
                               :min     1}}])

(def arealess-capacity-wind
  (assoc-in arealess-capacity
            [1 :name] (or
                       (cfg/snippet :common-parameter-inputs
                                    :arealess-capacity :name :wind)
                       "Kapazität für Offshore Windkraft in TWh")))

(def all-params-map
  (into {}
        (conj common-nrg-parameters
              energy-needed
              arealess-capacity-solar
              arealess-capacity-wind
              cap)))

(defn lookup-property [param-key property-key]
  (get-in all-params-map [param-key property-key]))

(defn granularize [param-key val]
  (* val (get-in all-params-map [param-key :granularity-factor] 1)))

(defn ungranularize [param-key val]
  (/ val (get-in all-params-map [param-key :granularity-factor] 1)))
