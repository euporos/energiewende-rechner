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

;; (t/deftest remix-energy-shares-int-test

;;   (t/is (=                              ; case without lockings
;;          (remix/remix-energy-shares-int
;;           :wind 5000
;;           {:wind
;;            {:share 4000
;;             :locked? false}
;;            :solar
;;            {:share 4000
;;             :locked? false}
;;            :nuclear
;;            {:share 500
;;             :locked? false}
;;            :bio
;;            {:share 1500
;;             :locked? false}})
;;          {:wind
;;           {:share 5000                  ; 4000 + 1000
;;            :locked? false}
;;           :solar
;;           {:share (- 4000 667)
;;            :locked? false}
;;           :nuclear
;;           {:share (- 500 83)
;;            :locked? false}
;;           :bio
;;           {:share (- 1500 250)
;;            :locked? false}}))

;;   (t/is (=                              ; case with lockings
;;          (remix/remix-energy-shares-int
;;           :wind 5000
;;           {:wind
;;            {:share 4000
;;             :locked? false}
;;            :solar
;;            {:share 4000
;;             :locked? false}
;;            :nuclear
;;            {:share 500
;;             :locked? false}
;;            :bio
;;            {:share 1500
;;             :locked? true}})
;;          {:wind
;;           {:share 5000                  ; 4000 + 1000
;;            :locked? false}
;;           :solar
;;           {:share (- 4000 889)
;;            :locked? false}
;;           :nuclear
;;           {:share (- 500 111)
;;            :locked? false}
;;           :bio
;;           {:share 1500
;;            :locked? true}}))

;;   (t/is (=                              ; case with reduction
;;          (remix/remix-energy-shares-int
;;           :wind 3000
;;           {:wind
;;            {:share 4000
;;             :locked? false}
;;            :solar
;;            {:share 4000
;;             :locked? false}
;;            :nuclear
;;            {:share 500
;;             :locked? false}
;;            :bio
;;            {:share 1500
;;             :locked? true}})
;;          {:wind
;;           {:share 3000                  ; 4000 - 1000
;;            :locked? false}
;;           :solar
;;           {:share (+ 4000 889)
;;            :locked? false}
;;           :nuclear
;;           {:share (+ 500 111)
;;            :locked? false}
;;           :bio
;;           {:share 1500
;;            :locked? true}}))
;;   ;; (t/is (= ; case where all racting shares are 0
;;   ;;        (remix/remix-energy-shares-int
;;   ;;         :wind 9000
;;   ;;         {:wind
;;   ;;          {:share 10000
;;   ;;           :locked? false}
;;   ;;          :solar
;;   ;;          {:share 0
;;   ;;           :locked? false}
;;   ;;          :nuclear
;;   ;;          {:share 0
;;   ;;           :locked? false}
;;   ;;          :bio
;;   ;;          {:share 0
;;   ;;           :locked? false}})
;;   ;;        {:wind
;;   ;;         {:share 9000 ; 10000 - 1000
;;   ;;          :locked? false}
;;   ;;         :solar
;;   ;;         {:share 333
;;   ;;          :locked? false}
;;   ;;         :nuclear
;;   ;;         {:share 333
;;   ;;          :locked? false}
;;   ;;         :bio
;;   ;;         {:share 334
;;   ;;          :locked? false}}))
;;   (t/is (=                  ; case where required share > 10000 (100%)
;;          (remix/remix-energy-shares-int
;;           :wind 10000
;;           {:wind
;;            {:share 90000
;;             :locked? false}
;;            :solar
;;            {:share 0
;;             :locked? false}
;;            :nuclear
;;            {:share 0
;;             :locked? false}
;;            :bio
;;            {:share 1000
;;             :locked? true}})
;;          {:wind
;;           {:share 9000
;;            :locked? false}
;;           :solar
;;           {:share 0
;;            :locked? false}
;;           :nuclear
;;           {:share 0
;;            :locked? false}
;;           :bio
;;           {:share 1000
;;            :locked? true}})))
