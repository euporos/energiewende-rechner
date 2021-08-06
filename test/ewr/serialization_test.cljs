(ns ewr.serialization-test
  (:require [cljs.test :as t :include-macros true]
            [ewr.serialization :as sut]))

(def teststate-badfloats
  {:energy-sources
   {:wind
    {:share             19.99305899851265
     :power-density     4.56
     :deaths            0.12
     :co2               11
     :resources         10260
     :arealess-capacity 240}
    :solar
    {:share             3.411403073872083
     :power-density     5.2
     :deaths            0.44
     :co2               44
     :resources         16447
     :arealess-capacity 142}
    :nuclear
    {:share         53
     :power-density 240.8
     :deaths        0.08
     :co2           12
     :resources     930}
    :bio
    {:share         0.5685671789786817
     :power-density 0.16
     :deaths        4.63
     :co2           230
     :resources     1080}
    :natural-gas
    {:share         3.411403073872083
     :power-density 482.1
     :deaths        2.82
     :co2           490
     :resources     572}
    :coal
    {:share         19.615567674764495
     :power-density 135.1
     :deaths        28.67
     :co2           820
     :resources     1185}}
   :energy-needed      1300
   :minor-energies-cap 40})

(def teststate-goodfloats
  {:energy-sources
   {:wind
    {:share             28
     :power-density     4.56
     :deaths            0.12
     :co2               11
     :resources         10260
     :arealess-capacity 240}
    :solar
    {:share             12
     :power-density     5.2
     :deaths            0.44
     :co2               44
     :resources         16447
     :arealess-capacity 142}
    :nuclear
    {:share         15
     :power-density 240.8
     :deaths        0.08
     :co2           12
     :resources     930}
    :bio
    {:share         2
     :power-density 0.16
     :deaths        4.63
     :co2           230
     :resources     1080}
    :natural-gas
    {:share         12
     :power-density 482.1
     :deaths        2.82
     :co2           490
     :resources     572}
    :coal
    {:share         31
     :power-density 135.1
     :deaths        28.67
     :co2           820
     :resources     1185}}
   :energy-needed      1300
   :minor-energies-cap 40})

127

(count (str
        (sut/serialize-and-compress teststate-goodfloats)))

(time
 (count (str
         (sut/serialize-and-compress teststate-badfloats))))

(count "Ai-gENVkaXkxSaeE7C76BijmLyq1VpRVdpWH0COwvLXnamK4LfQi8lKsoKwLp230DFVok2JaKrnRuw-gI0iHJsNKO1o6Sw3ve")

(count "bYPjWoUOgjOrSXcvQeIeyNtIGknRnDOWJmrLNGRthiH0U6ylpFANkak6sYdCCIDl5bI20hklXEokhqOBVRkaC5YA1cQqFWEnLsgsYw")

(count "CrvgA6xjekzTyqlbpx2xgU8vLzLNGaUjNZKZxgVfHbKzOsp5b3YwHmaSMUuj3crXGBTxlc1HKvGt-X2OMC6pV15qOpSyK_KpbxEQ")

"[1300 [[28 4.56 0.12 11 10260 240] [12 5.2 0.44 44 16447 142] [15 240.8 0.08 12 930] [2 0.16 4.63 230 1080] [12 482.1 2.82 490 572] [31 135.1 28.67 820 1185]]]"
