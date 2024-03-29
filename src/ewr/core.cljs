(ns ewr.core
  (:require
   [clojure.pprint :refer [pprint]]
   [ewr.config :as config]
   [ewr.reframing]
   [ewr.views :as views]
   [re-frame.core :as re-frame]
   [reagent.dom :as rdom])
  (:require-macros [ewr.macros :as m]))

(def root-el
  (.getElementById js/document "app"))

(m/def-string-from-file version
  "resources/version.txt" str)

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (rdom/unmount-component-at-node root-el)
  (rdom/render [views/main-component] root-el))

(defn ^:dev/after-load re-register-tracks []
  (re-frame/dispatch-sync [:global/dispose-tracks])
  (re-frame/dispatch-sync [:global/register-tracks]))

(defn init []
  (print "EWR version " version)
  (print "Built with settings: " (pprint config/settings))
  (re-frame/dispatch-sync [:global/initialize :load-savestate])
  (mount-root)
  (re-frame/dispatch-sync [:global/register-tracks]))

