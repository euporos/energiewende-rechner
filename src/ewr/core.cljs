(ns ewr.core
  (:require
   [ewr.reframing]
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [ewr.views :as views]
   [ewr.config :as config])
  (:require-macros [ewr.macros :as m]))

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
  (print "Ewr.version " version)
  (re-frame/dispatch-sync [:global/initialize])
  (mount-root))
 
  
