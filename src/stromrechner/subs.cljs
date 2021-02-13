(ns stromrechner.subs
  (:require
   [re-frame.core :as rf :refer [reg-sub]]))

(reg-sub
 ::name
 (fn [db]
   (:name db)))




(reg-sub
 ::energy-sources
 (fn [db]
   (:energy-sources db)))


