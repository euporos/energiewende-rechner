(ns ewr.serialization
  (:require [ewr.serialization-delta :as delta]
            [ewr.serialization-general :as general]))

(def serializer-version 1)

(defn serialize-and-compress [savestate]
  (->> [(general/serialize-and-compress savestate)
        (delta/encode savestate)]
       (sort-by count)
       first))

(defn decompress-and-deserialize [compressed-savestate]
  (try
    (if-let [preset-decoding (delta/decode compressed-savestate)]
      preset-decoding
      (general/decompress-and-deserialize compressed-savestate))
    (catch js/Object _e
      (js/console.log "Error reading savestate… not loading")
      nil)))
