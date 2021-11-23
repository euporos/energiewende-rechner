(ns stromrechner.logic-test
  (:require [stromrechner.logic :as logic]
            [cljs.test :as t :include-macros true]))

(t/deftest remix-unblocked?-test
  (t/is (not (logic/remix-blocked?
              :wind 2000
              {:wind
               {:share 4000
                :locked? false}
               :solar
               {:share 4000
                :locked? false}
               :nuclear
               {:share 500
                :locked? false}
               :bio
               {:share 1500
                :locked? false}})))
  (t/is (not
         (logic/remix-blocked?
          :wind 2000
          {:wind
           {:share 4000
            :locked? false}
           :solar
           {:share 4000
            :locked? true}
           :nuclear
           {:share 500
            :locked? false}
           :bio
           {:share 1500
            :locked? false}})))
  (t/is (logic/remix-blocked? ; changed value blocked
         :wind 200
         {:wind
          {:share 4000
           :locked? true}
          :solar
          {:share 4000
           :locked? false}
          :nuclear
          {:share 500
           :locked? false}
          :bio
          {:share 1500
           :locked? false}}))
  (t/is (logic/remix-blocked? ; all but the changed value blocked
         :wind 2000
         {:wind
          {:share 4000
           :locked? false}
          :solar
          {:share 4000
           :locked? true}
          :nuclear
          {:share 500
           :locked? true}
          :bio
          {:share 1500
           :locked? true}})))

(t/deftest remix-energy-shares-int-test

  (t/is (= ; case without lockings
         (logic/remix-energy-shares
          :wind 5000
          {:wind
           {:share 4000
            :locked? false}
           :solar
           {:share 4000
            :locked? false}
           :nuclear
           {:share 500
            :locked? false}
           :bio
           {:share 1500
            :locked? false}})
         {:wind
          {:share 5000 ; 4000 + 1000
           :locked? false}
          :solar
          {:share (- 4000 667)
           :locked? false}
          :nuclear
          {:share (- 500 83)
           :locked? false}
          :bio
          {:share (- 1500 250)
           :locked? false}}))

  (t/is (= ; case with lockings
         (logic/remix-energy-shares
          :wind 5000
          {:wind
           {:share 4000
            :locked? false}
           :solar
           {:share 4000
            :locked? false}
           :nuclear
           {:share 500
            :locked? false}
           :bio
           {:share 1500
            :locked? true}})
         {:wind
          {:share 5000 ; 4000 + 1000
           :locked? false}
          :solar
          {:share (- 4000 889)
           :locked? false}
          :nuclear
          {:share (- 500 111)
           :locked? false}
          :bio
          {:share 1500
           :locked? true}}))

  (t/is (= ; case with reduction
         (logic/remix-energy-shares
          :wind 3000
          {:wind
           {:share 4000
            :locked? false}
           :solar
           {:share 4000
            :locked? false}
           :nuclear
           {:share 500
            :locked? false}
           :bio
           {:share 1500
            :locked? true}})
         {:wind
          {:share 3000 ; 4000 - 1000
           :locked? false}
          :solar
          {:share (+ 4000 889)
           :locked? false}
          :nuclear
          {:share (+ 500 111)
           :locked? false}
          :bio
          {:share 1500
           :locked? true}}))
  ;; (t/is (= ; case where all racting shares are 0
  ;;        (logic/remix-energy-shares
  ;;         :wind 9000
  ;;         {:wind
  ;;          {:share 10000
  ;;           :locked? false}
  ;;          :solar
  ;;          {:share 0
  ;;           :locked? false}
  ;;          :nuclear
  ;;          {:share 0
  ;;           :locked? false}
  ;;          :bio
  ;;          {:share 0
  ;;           :locked? false}})
  ;;        {:wind
  ;;         {:share 9000 ; 10000 - 1000
  ;;          :locked? false}
  ;;         :solar
  ;;         {:share 333
  ;;          :locked? false}
  ;;         :nuclear
  ;;         {:share 333
  ;;          :locked? false}
  ;;         :bio
  ;;         {:share 334
  ;;          :locked? false}}))
  (t/is (= ; case where required share > 10000 (100%)
         (logic/remix-energy-shares
          :wind 10000
          {:wind
           {:share 90000
            :locked? false}
           :solar
           {:share 0
            :locked? false}
           :nuclear
           {:share 0
            :locked? false}
           :bio
           {:share 1000
            :locked? true}})
         {:wind
          {:share 9000
           :locked? false}
          :solar
          {:share 0
           :locked? false}
          :nuclear
          {:share 0
           :locked? false}
          :bio
          {:share 1000
           :locked? true}})))

(t/deftest remix-energy-shares-float-test
  (t/is (=
         (logic/remix-energy-shares-float
          :wind 50
          {:wind
           {:share 40
            :locked? false}
           :solar
           {:share 40
            :locked? false}
           :nuclear
           {:share 5
            :locked? false}
           :bio
           {:share 15
            :locked? false}})
         {:wind
          {:share 50 ; 40+10
           :locked? false}
          :solar
          {:share (* 50 (/ 40 60))
           :locked? false}
          :nuclear
          {:share (* 50 (/ 5 60))
           :locked? false}
          :bio
          {:share (* 50 (/ 15 60))
           :locked? false}}) "case without lockings")
  (t/is (=
         (logic/remix-energy-shares-float
          :wind 50
          {:wind
           {:share 40
            :locked? false}
           :solar
           {:share 40
            :locked? true}
           :nuclear
           {:share 5
            :locked? false}
           :bio
           {:share 15
            :locked? false}})
         {:wind
          {:share 50 ; 40+10
           :locked? false}
          :solar
          {:share 40
           :locked? false}
          :nuclear
          {:share (* 10 (/ 5 20))
           :locked? false}
          :bio
          {:share (* 10 (/ 15 20))
           :locked? false}}) "case with lockings")
  (t/is (=
         (logic/remix-energy-shares-float
          :wind 100
          {:wind
           {:share 40
            :locked? false}
           :solar
           {:share 40
            :locked? true}
           :nuclear
           {:share 5
            :locked? false}
           :bio
           {:share 15
            :locked? false}})
         {:wind
          {:share 100
           :locked? false}
          :solar
          {:share 0
           :locked? false}
          :nuclear
          {:share 0
           :locked? false}
          :bio
          {:share 0
           :locked? false}}) "change one energy to 100 (avoid div by 0")
  (t/is (=
         (logic/remix-energy-shares-float
          :wind 88
          {:wind
           {:share 100
            :locked? false}
           :solar
           {:share 0
            :locked? false}
           :nuclear
           {:share 0
            :locked? false}
           :bio
           {:share 0
            :locked? false}})
         {:wind
          {:share 88
           :locked? false}
          :solar
          {:share 4
           :locked? false}
          :nuclear
          {:share 4
           :locked? false}
          :bio
          {:share 4
           :locked? false}}) "raise reacting energies equally from 0"))


