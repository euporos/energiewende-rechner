(ns prerender.main
  (:require
   [ewr.reframing :as rfr]
   [ewr.views :as views]
   [re-frame.core :as rf]
   [reagent.dom.server :as rdom]))


(defn prerender
  ""
  []
  (rf/dispatch-sync [:global/load-default-pubs])
  (print (rdom/render-to-string
          [views/main-component])))
