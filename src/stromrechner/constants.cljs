(ns stromrechner.constants)

(def hours-per-year (* 24 365))

(def parameters
  [[:power-density {:name "Ø-Leistung in W/m²"
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
             :input-attrs {:type "number"
                           :pattern "0.00"
                           :step "0.01"
                           :min 0.01}}]
   [:co2 {:name [:span "CO" [:sub "2"] "-Äquivalent: " ]
          :unit "kt/TWh"
          :abs-unit "kt"
          :parse-fn js/parseInt
          :input-attrs {:type "number"
                        :pattern "0"
                        :step "1"
                        :min 1}}]])

(def energy-needed
  [:energy-needed {:name "Strombedarf"
                   :unit "TWh"
                   :parse-fn js/parseFloat
                   :input-attrs {:type "number"
                                 :pattern "1"
                                 :step "1"
                                 :min 0}}])


(def arealess-capacity
  [:arealess-capacity {:name "Solarkapazität Dächer"
                         :unit "TWh"
                         :parse-fn js/parseFloat
                         :input-attrs {:type "number"
                                       :pattern "1"
                                       :step "1"
                                       :min 0}}])

(def parameter-map (into {} parameters))



;; (def share-granularity 10000) ; 100% ≙ 10000

;; (def deaths-granularity 1000) ; 100% ≙ 1000
 
