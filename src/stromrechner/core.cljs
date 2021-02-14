(ns stromrechner.core
  (:require
   [stromrechner.db-interaction :refer [load-default-pubs]]
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [stromrechner.views :as views]
   [stromrechner.config :as config]
   ))

(def version "0.31")

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [:global/initialize-db])
  (print "Stromrechner version " version)
  (load-default-pubs)
  (dev-setup)
  (mount-root))
 

   

