(ns ewr.remix-test
  (:require [cljs.test :as t :include-macros true]
            [ewr.remix :as remix]))

(t/deftest distribute-energy
  (t/is (= (remix/distribute-energy
            60
            {:wind {:share 100}
             :solar {:share 50}
             :nuclear {:share 50}})
           {:wind {:share 130}
            :solar {:share 65}
            :nuclear {:share 65}})))

