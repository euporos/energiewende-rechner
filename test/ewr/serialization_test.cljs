(ns ewr.serialization-test
  (:require [cljs.test :as t :include-macros true]
            [clojure.data :as cd]
            [ewr.config :as cfg]
            [ewr.serialization :as serialize]
            [ewr.testdata :refer [preset-1 preset-2]]))

(t/deftest encode-decode-roundtrip
  (t/is (= preset-1
           (serialize/decode (serialize/encode preset-1))))

  (t/is (= preset-2
           (serialize/decode (serialize/encode preset-2))))

  (t/is (= preset-1
           (serialize/string->savestate (serialize/savestate->string preset-1)))))

