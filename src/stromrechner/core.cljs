(ns stromrechner.core
  (:require
   [stromrechner.reframing]
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [stromrechner.views :as views]
   [stromrechner.config :as config])
  (:require-macros [stromrechner.macros :as m]))

(goog-define config-dir "config")

(def root-el
  (.getElementById js/document "app"))

(m/def-string-from-file version
  "resources/version.txt" str)

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (rdom/unmount-component-at-node root-el)
  (rdom/render [views/main-component] root-el))

(defn init []
  (print "Stromrechner version " version)
  (re-frame/dispatch-sync [:global/initialize])
  (mount-root))
 
  
