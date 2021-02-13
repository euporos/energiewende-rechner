(ns stromrechner.constants)

(def hours-per-year (* 24 365))

(def parameters
  [[:power-density {:name "Leistungsdichte"
                    :unit "W/m²"
                    :parse-fn js/parseFloat
                    :input-attrs {:type "number"
                                  :pattern "0.00"
                                  :step "0.01"
                                  :min 0.01}}]
   [:capacity-factor {:name "Kapazitätsfaktor"
                      :unit "1=100%"
                      :parse-fn js/parseFloat
                      :input-attrs {:type "number"
                                    :pattern "0.00"
                                    :step "0.01"
                                    :min 0.01
                                    :max 1}}]
   [:deaths {:name "Todesfälle/TWh"
             :unit "/TWh"
             :parse-fn js/parseFloat
             :input-attrs {:type "number"
                           :pattern "0.00"
                           :step "0.01"
                           :min 0.01}}]])

(def energy-needed
  [:energy-needed {:name "Strombedarf"
                   :unit "TWh"
                   :parse-fn js/parseFloat
                   :input-attrs {:type "number"
                                 :pattern "1"
                                 :step "1"
                                 :min 0}}])



(def share-granularity 10000) ; 100% ≙ 10000

(def deaths-granularity 1000) ; 100% ≙ 1000

 
;; (def energy-sources
;;   {:wind {:name "Wind"
;;           :props {:cx 400
;;                   :cy 250
;;                   :fill "rgba(135, 206, 250,0.6)" ; lightskyblue
;;                   }} 
;;    :solar {:name "Sonne"
;;            :props {:cx 350
;;                    :cy 700
;;                    :fill "rgbuia(255, 255, 0, 0.6)"}}
;;    :nuclear {:name "Kernenergie"
;;           :props {:cx 130
;;                   :cy 450
;;                   :fill "rgba(250, 147, 38, 0.5)"}}
;;    :bio {:name "Biogas"
;;          :props {:cx 320
;;                  :cy 450
;;                  :fill "rgba(50, 205, 50, 0.5)"}}})
