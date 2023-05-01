(ns ewr.derivations-test
  #_(:require  [cljs.test :as t :include-macros true]
               [ewr.reframing :as rfr]
               [pjstadig.humane-test-output]))

;; (def energy-needed
;;   1300)

;; (def nrgs
;;   {:wind
;;    {:share 80
;;     :locked? false
;;     :power-density 6.37
;;     :capacity-factor 0.3
;;     :deaths 0.12
;;     :co2 24000}
;;    :solar
;;    {:share 20
;;     :locked? false
;;     :power-density 6.32
;;     :capacity-factor 1
;;     :deaths 0.44
;;     :co2 101000}})

;; (t/deftest enrich-data-for-indicator-test
;;   (t/is (= {:param-total 239.2
;;             :unit nil
;;             :energy-sources
;;             {:wind
;;              {:share 80
;;               :locked? false
;;               :power-density 6.37
;;               :capacity-factor 0.3
;;               :deaths 0.12
;;               :co2 24000
;;               :absolute 124.8
;;               :param-share 52.17391304347826}
;;              :solar
;;              {:share 20
;;               :locked? false
;;               :power-density 6.32
;;               :capacity-factor 1
;;               :deaths 0.44
;;               :co2 101000
;;               :absolute 114.4
;;               :param-share 47.82608695652175}}}
;;            (rfr/enrich-data-for-indicator [1300 nrgs] [nil :deaths]))))



