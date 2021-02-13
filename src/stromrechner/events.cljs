(ns stromrechner.events
  (:require
   [re-frame.core :as rf :refer [reg-event-db]]
   [stromrechner.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   ))

