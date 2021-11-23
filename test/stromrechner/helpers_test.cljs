(ns stromrechner.helpers-test
  (:require [stromrechner.helpers :as h]
            [cljs.test :as t :include-macros true]))

(t/deftest reverse-paths-test
  (t/is (= {:biogas
            {:flaechenverbrauch 120
             :vollast 0.8
             :tote 2}
            :wind
            {:flaechenverbrauch 40
             :vollast 0.45
             :tote 0.1}
            :solar
            {:flaechenverbrauch 140
             :vollast 0.33
             :tote 0.4}
            :kern
            {:flaechenverbrauch 0.1
             :vollast 0.85
             :tote 0.9}}
           (h/reverse-paths
            {:flaechenverbrauch
             {:biogas 120
              :wind 40
              :solar 140
              :kern 0.1}
             :vollast
             {:biogas 0.8
              :solar 0.33
              :wind 0.45
              :kern 0.85}
             :tote
             {:biogas 2
              :solar 0.4
              :wind 0.1
              :kern 0.9}}))))


