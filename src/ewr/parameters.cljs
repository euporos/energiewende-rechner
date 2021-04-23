(ns ewr.parameters
  (:require [ewr.helpers :as h]
            [ewr.config :as cfg]))

;; ###########################
;; ###### Energy Needed ######
;; ###########################

(def energy-needed
  [:energy-needed {:name "Strombedarf"
                   :unit "TWh"
                   :parse-fn js/parseFloat
                   :input-attrs {:type "number"
                                 :pattern "1"
                                 :step "1"
                                 :min 0}}])


;; ##############################################
;; ########## Common Energy-parameters ##########
;; ##############################################

;; these Parameters must be defined for all parameters
 
(def common-nrg-parameters
  (map (fn [[param-key param-dfn]]
         ;; Overrides with the snippets
         ;; from the config file
         [param-key (merge                     
                     param-dfn
                     (cfg/snippet :common-parameter-inputs param-key))])
   [[:power-density {:name "Bemessungsleistung pro m² in W"
                     :unit "W/m²"
                     :parse-fn js/parseFloat
                     :input-attrs {:type "number"
                                   :pattern "0.00"
                                   :step "0.01"
                                   :min 0.01}}]
   
    ;; [:capacity-factor {:name "Kapazitätsfaktor"
    ;;                    :unit "1=100%"
    ;;                    :parse-fn js/parseFloat
    ;;                    :input-attrs {:type "number"
    ;;                                  :pattern "0.00"
    ;;                                  :step "0.01"
    ;;                                  :min 0.01
    ;;                                  :max 1}}]
   
    [:deaths {:name "Todesfälle/TWh"
              :unit "/TWh"
              :parse-fn js/parseFloat
              :indicator-formatter #(h/structure-int
                                     (Math/round %))
              :input-attrs {:type "number"
                            :pattern "0.00"
                            :step "0.01"
                            :min 0.01}}]
   
    [:co2 {:name [:span "Spezifische CO" [:sub "2"] "-Emiss. in g/kWh" ]
           :unit "g/kWh"
           :indicator-formatter #(-> %
                                     (* 0.001) ; convert to Mio t
                                     (* 10)
                                     Math/round
                                     (/ 10))
           :abs-unit "Mio. t"
           :parse-fn js/parseInt
           :input-attrs {:type "number"
                         :pattern "0"
                         :step "1"
                         :min 1}}]
   
    [:resources {:name "Ressourcenverbrauch in t/TWh"
                 :unit "t/TWh"
                 :abs-unit "kt"
                 :parse-fn js/parseFloat
                 :indicator-formatter #(-> %
                                           (* 0.001) ; convert to Mio t                                    
                                           Math/round                                    
                                           (h/structure-int))
                 :input-attrs {:type "number" 
                               :pattern "0.00"
                               :step "0.01"
                               :min 0.01}}]]))



(def common-parameter-map (into {} common-nrg-parameters))

(def common-param-keys (map first common-nrg-parameters))

;; ###############################################
;; ########## Special Energy Parameters ##########
;; ###############################################

;; Parameters that follow the general pattern,
;; but are not defined for all Energy-sources
;; currently there's only one

(def arealess-capacity
  [:arealess-capacity {:unit "TWh"
                       :parse-fn js/parseFloat
                       :input-attrs {:type "number"
                                     :pattern "1"
                                     :step "1"
                                     :min 0}}])

(def arealess-capacity-solar
  (assoc-in arealess-capacity
            [1 :name] (or
                       (cfg/snippet :common-parameter-inputs
                                    :arealess-capacity :name :solar)
                       "Solarkapazität auf Dächern in TWh"))) 

(def arealess-capacity-wind
  (assoc-in arealess-capacity
            [1 :name] (or
                       (cfg/snippet :common-parameter-inputs
                                    :arealess-capacity :name :wind)
                       "Kapazität für Offshore Windkraft in TWh")))


