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
            :nuclear {:share 65}}))

  (t/is (= (remix/distribute-energy
            -60
            {:wind {:share 100}
             :solar {:share 50}
             :nuclear {:share 50}})
           {:wind {:share 70}
            :solar {:share 35}
            :nuclear {:share 35}}))

  (t/is (= (remix/distribute-energy
            -200
            {:wind {:share 100}
             :solar {:share 50}
             :nuclear {:share 50}})
           {:wind {:share 0}
            :solar {:share 0}
            :nuclear {:share 0}})) 1

  (t/is (= (remix/distribute-energy
            50
            {:wind {:share 100}
             :solar {:share 50}
             :nuclear {:share 50}})
           {:wind {:share 125}
            :solar {:share 63}
            :nuclear {:share 62}})))

(t/deftest remix-int
  (t/is (= (remix/remix-energy-shares-int
            :coal
            40
            {:coal {:share 100}
             :wind {:share 100}
             :solar {:share 50}
             :nuclear {:share 50}})
           {:coal {:share 40}
            :wind {:share 130}
            :solar {:share 65}
            :nuclear {:share 65}}))

  (t/is (= (remix/remix-energy-shares-int
            :coal
            40
            {:coal {:share 100}
             :wind {:share 100}
             :solar {:share 50
                     :locked? true}
             :nuclear {:share 50}})
           {:coal {:share 40}
            :wind {:share 140}
            :solar {:share 50
                    :locked? true}
            :nuclear {:share 70}})))
