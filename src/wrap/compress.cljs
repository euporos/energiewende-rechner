(ns wrap.compress
  (:require [lzutf8]))

(defn compress-b64
  ""
  [input]
  (.compress
   lzutf8
   (str input)
   (clj->js {:outputEncoding "Base64"})))

(defn decompress-b64
  ""
  [input]
  (.decompress
   lzutf8
   (str input)
   (clj->js {:inputEncoding "Base64"})))
