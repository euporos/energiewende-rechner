(ns stromrechner.config
  (:require-macros [stromrechner.macros :as m]))

(def debug?
  ^boolean goog.DEBUG)


(m/def-from-file settings
  "config/settings.edn" identity)
  
