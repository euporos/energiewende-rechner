(ns stromrechner.derivations-test
  (:require  [cljs.test :as t :include-macros true]
             [stromrechner.logic :as l]
             [pjstadig.humane-test-output]))

(def energy-needed
  1300)

(def nrgs
  {:wind
   {:share 80
    :locked? false
    :power-density 6.37
    :capacity-factor 0.3
    :deaths 0.12
    :co2 24000}
   :solar
   {:share 20
    :locked? false
    :power-density 6.32
    :capacity-factor 1
    :deaths 0.44
    :co2 101000}})


(t/deftest derive-absolute-deaths-test
  (t/is (= {:total-deaths 239.2
            :energy-sources
            {:wind
             {:share 80
              :locked? false
              :power-density 6.37
              :capacity-factor 0.3
              :deaths 0.12
              :co2 24000
              :absolute-deaths 124.8
              :deaths-share 52.17391304347826}
             :solar
             {:share 20
              :locked? false
              :power-density 6.32
              :capacity-factor 1
              :deaths 0.44
              :co2 101000
              :absolute-deaths 114.4
              :deaths-share 47.82608695652175}}}
           (l/derive-share-absolutes-and-total [1300 nrgs] [nil :deaths]))))


