(ns ewr.core
  (:require
   [ewr.reframing]
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [ewr.views :as views]
   [ewr.config :as config]
   [clojure.string :as str])
  (:require-macros [ewr.macros :as m]))

(def root-el
  (.getElementById js/document "app"))

(m/def-string-from-file version
  "resources/version.txt" str)

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (rdom/unmount-component-at-node root-el)
  (rdom/render [views/main-component] root-el))

(defn init []
  (print "EWR version " version)
  (js/console.log "Compiled with the following features: " (str/join ", " (:features config/settings)))
  (re-frame/dispatch-sync [:global/initialize])
  (mount-root))
