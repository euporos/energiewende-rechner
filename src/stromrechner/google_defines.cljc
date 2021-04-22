(ns stromrechner.google-defines
  (:require [clojure.string :as str]))

(goog-define config-dir "config")

(defn in-cfg-dir
  "makes sure there is exactly one slash /
  between cfg-dir and path."
  [path]
  (str
   (str/replace config-dir #"/$" "") "/"
   (str/replace path #"^/" "")))
