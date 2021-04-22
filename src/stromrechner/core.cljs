(ns stromrechner.core
  (:require
   [stromrechner.reframing]
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [stromrechner.views :as views]
   [stromrechner.config :as config])
  (:require-macros [stromrechner.macros :as m]))

(def root-el
  (.getElementById js/document "app"))

(m/def-string-from-file version
  "resources/version.txt" str)

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (rdom/unmount-component-at-node root-el)
  (rdom/render [views/main-component] root-el))

(defn init []
  (re-frame/dispatch-sync [:global/initialize])
  ;; (re-frame/dispatch [:save/load-savestate-from-url])
  (print "Stromrechner version " version)
  ;; (re-frame/dispatch [:nrg/load-pub-defaults])  
  ;; (set! (.-innerHTML root-el) nil)
  (mount-root))
 
  
