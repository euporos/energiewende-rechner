(ns stromrechner.constants
  (:require [stromrechner.helpers :as h]))

(def hours-per-year (* 24 365))

(def parameters
  [[:power-density {:name "Bemessungsleistung pro m² in W"
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
   [:co2 {:name [:span "CO" [:sub "2"] "-Äquivalent in g/kWh" ]
          :unit "g/kWh"
          :indicator-formatter #(-> %
                                    (* 0.001) ; convert to Mio t
                                    (* 10)
                                    Math/round
                                    (/ 10))
          :abs-unit "Mio. t"
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
                              :min 0.01}}]])

(def param-keys (map first parameters))

(def energy-needed
  [:energy-needed {:name "Strombedarf"
                   :unit "TWh"
                   :parse-fn js/parseFloat
                   :input-attrs {:type "number"
                                 :pattern "1"
                                 :step "1"
                                 :min 0}}])

(def arealess-capacity
  [:arealess-capacity {:name "Solarkapazität auf Dächern in TWh"
                         :unit "TWh"
                         :parse-fn js/parseFloat
                         :input-attrs {:type "number"
                                       :pattern "1"
                                       :step "1"
                                       :min 0}}])

(def parameter-map (into {} parameters))

(def area-germany 357581)



;; (def share-granularity 10000) ; 100% ≙ 10000

;; (def deaths-granularity 1000) ; 100% ≙ 1000
 
