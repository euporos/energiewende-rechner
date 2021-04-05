(ns stromrechner.db-interaction
  (:require
   [re-frame.core :as rf :refer [reg-event-db reg-sub]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as r]
   [reagent.ratom :as ratom]
   [stromrechner.sources :as sources]
   [clojure.edn :as edn]
   [stromrechner.color :as color]
   [stromrechner.logic :as logic]
   [stromrechner.constants :as constants]
   [stromrechner.constants :as const]
   [stromrechner.helpers :as h]
   [stromrechner.config :as cfg]
   [stromrechner.logic :as l]
   [thi.ng.color.core :as col]))


;; ########################
;; ##### Global Stuff #####
;; ########################

(def default-db
  {:energy-sources
   (get cfg/settings :init-mix)})

(rf/reg-event-db
 :global/initialize-db
 (fn-traced [_ _] 
            default-db))
 
(reg-sub
 :global/db
 (fn [db] db))


(reg-sub
 :global/energy-needed
 (fn [db _]
   (get db :energy-needed)))

(reg-sub
 :global/energy-keys
 (fn [db]
   (keys (:energy-sources db))))

(reg-sub
 :global/get-path
 (fn [db [_ path]]
   (get-in db path)))

(reg-event-db
 :global/set-path
 (fn [db [_ path newval]]))

;; ############
;; ### NRGS ###
;; ############

(def nrg-consts
  (get cfg/settings :nrg-constants))

(reg-sub
 :global/energy-sources
 (fn [db]
   (merge-with merge
               nrg-consts
               (:energy-sources db))))

(reg-sub
 :nrg/get
 (fn [_]
   (rf/subscribe [:global/energy-sources]))
 (fn [nrgs [_ nrg-key]]
   (get nrgs nrg-key)))



(reg-sub
 :nrg/get-param
 (fn [_]
   (rf/subscribe [:global/energy-sources]))
 (fn [nrgs [_ nrg-key param]]
   (get-in nrgs [nrg-key param])))

(reg-sub
 :nrg/exhausted-arealess
 (fn [[_ nrg-key]]
   [(rf/subscribe [:nrg/get-param nrg-key :arealess-capacity])
    (rf/subscribe [:nrg-share/get-abs nrg-key])])
 (fn [[arealess-capacity twh-share] [_ nrg-key]]
   (if (> twh-share arealess-capacity)
     arealess-capacity
     twh-share)))



(comment
  @(rf/subscribe [:nrg/get-param :wind :arealess-capacity])
  @(rf/subscribe [:nrg/exhausted-arealess :wind]))

;; ######################
;; ##### Parameters #####
;; ######################

(rf/reg-event-db
 :params/load-pub
 (fn [db [_ pub nrg-key param]]
   (assoc-in db [:energy-sources nrg-key param]
             (get-in pub [:energy-sources nrg-key param]))))


(reg-event-db
 :param/set-unparsed
 (fn [db [_ prepath [param-key {:keys [parse-fn]}]
          unparsed-newval]]
   (assoc-in db (conj prepath param-key)
             (parse-fn unparsed-newval))))


(reg-sub :param/get
         (fn [db [_ pre-path param-key]]           
           (get-in db (conj pre-path param-key))))


;; ########################
;; ##### Publications #####
;; ########################

;; ### Power needed

(reg-event-db
 :energy-needed/set
 (fn [db [_ newval]]
   (assoc db :energy-needed newval)))

(reg-event-db
 :energy-needed/load
 (fn [db [_ pub]]
   (if (not= pub nil)
     (-> db
         (assoc :energy-needed
                (get pub :energy-needed))
         (assoc-in [:ui :loaded-pubs :energy-needed]
                   (:id pub))))))


(defn- return-loaded-pub 
  ""
  [matching-pubs last-loaded]
  (case (count matching-pubs)
       0 nil
       1 (first matching-pubs)
       (first (filter #(= (:id %) last-loaded) ; in case there is more than one
                      matching-pubs)))) ; pub with identical values

(reg-sub
 :energy-needed/loaded
 (fn [db _]
   (let [curval (get db :energy-needed)
         matching-pubs (sources/matching-pubs-for-path [:energy-needed] curval)
         last-loaded (get-in db [:ui :loaded-pubs :energy-needed])]
     (return-loaded-pub matching-pubs last-loaded))))


;; ### Parameters

(reg-event-db
 :pub/load
 (fn [db [_ nrg-key param-key pub]]
   (if (not= pub nil)
     (-> db
         (assoc-in [:energy-sources nrg-key param-key]
                   (get-in pub [:energy-sources nrg-key param-key]))
         (assoc-in [:ui :loaded-pubs nrg-key param-key]
                   (:id pub))))))


(reg-sub
 :pub/loaded
 (fn [db [_ nrg-key param-key]]
   (let [curval (get-in db [:energy-sources nrg-key param-key])
         matching-pubs (sources/matching-pubs nrg-key param-key curval)
         last-loaded (get-in db [:ui :loaded-pubs nrg-key param-key])]
     (return-loaded-pub matching-pubs last-loaded))))

(defn load-default-pubs
  ""
  []
  (rf/dispatch [:energy-needed/load
                (first (sources/pubs-for-needed-power))])
  (doseq [nrg-key (map first (:energy-sources default-db))
          param-key (map first constants/parameters)]
    (rf/dispatch [:pub/load nrg-key param-key
                  (sources/default-pub nrg-key param-key)]))
  (rf/dispatch [:pub/load :solar :arealess-capacity
                (sources/default-pub :solar :arealess-capacity)])
  (rf/dispatch [:pub/load :wind :arealess-capacity
                  (sources/default-pub :wind :arealess-capacity)]))

;; ###########################
;; ###### Energy shares ######
;; ###########################

(reg-sub
 :nrg/locked?
 (fn [db [_ nrg-key]]
   (if (get-in db [:energy-sources nrg-key :locked?])
     true false)))

(reg-event-db
 :nrg/toggle-lock
 (fn [db [_ nrg-key]]
   (update-in db [:energy-sources nrg-key :locked?] not)))

(reg-sub
 :nrg-share/get
 (fn [db [_ nrg-key]]
   (get-in db [:energy-sources nrg-key :share])))

(reg-sub
 :nrg-share/get-abs
 (fn [[_ nrg-key]]
   [(rf/subscribe [:global/energy-needed])
    (rf/subscribe [:nrg-share/get nrg-key])])
 (fn [[energy-needed share] [_ nrg-key]]
   (-> share
       (/ 100)
       (* energy-needed))))

 

(comment
  @(rf/subscribe [:global/energy-sources])
  @(rf/subscribe [:share/fossil-share])
  @(rf/subscribe [:ui/decab-color]))

(comment
  @(rf/subscribe [:global/energy-needed])
  @(rf/subscribe [:nrg-share/get-abs :wind]))

(reg-event-db
 :nrg-share/remix
 (fn [db [_ nrg-key newval]]
   (update db :energy-sources
           #(logic/attempt-remix nrg-key newval %))))


;; ############################
;; ###### Derived-values ######
;; ############################
  

(comment
  @(rf/subscribe [:nrg/get :wind])
  @(rf/subscribe [:global/energy-needed]))


(defn radius-from-area-circle
  ""
  [surface]
  (Math/sqrt (/ surface Math/PI)))

(reg-sub
 :deriv/surface-added
 (fn [[_ nrg-key]]
   [(rf/subscribe [:global/energy-needed])
    (rf/subscribe [:nrg/get nrg-key])])
 (fn [[energy-needed nrg] [_ nrg-key]]
   (let [{:keys [share power-density props
                 capacity-factor deaths] :as nrg} nrg
         area (-> energy-needed
                     (* share)
                     (/ 100) ; share in TWh ;TODO: from constant
                     (- (:arealess-capacity nrg 0))
                     (* 1000000000000) ; share in Wh
                     (/ const/hours-per-year) ; needed netto W
                     ;; (/ capacity-factor)
                                        ; needed brute W
                     (/ power-density) ; needed m²
                     (/ 1000000)) ; needed km²
         radius (if (or (< area 0) ; area < 0 possible with arealess-capacity
                     (js/isNaN area)) 0
                    (radius-from-area-circle area))]
     (assoc nrg
            :area area
            :relative-area (/ area const/area-germany)
            :radius radius
            :diameter (* 2 radius )))))



(defn enrich-data-for-indicator
  [[energy-needed energy-sources] [_ param-key]]
  (let [abs-added (h/map-vals
                    #(assoc % :absolute
                            (-> (:share %)
                                (/ 100)            ;TODO: from const
                                (* energy-needed)  ; TWh of this nrg                                
                                (* (param-key %))))
                    energy-sources)
         total (reduce #(+ %1 (:absolute (second %2)))
                       0 abs-added)
         shares-added (h/map-vals
                       #(assoc % :param-share
                               (-> (:absolute %)
                                   (/ total)
                                   (* 100)
                                   (h/nan->0))) ;TODO: from const
                       abs-added)]
    {:param-total total
     :factor (get-in const/parameter-map [param-key :indicator-factor] 0)
     :formatter (get-in const/parameter-map
                        [param-key :indicator-formatter] #(Math/round %))
     :unit (get-in const/parameter-map [param-key :abs-unit])
     :energy-sources shares-added}))


(reg-sub ; param-key should be :co2 or :deaths
 :deriv/data-for-indicator
 (fn [[_ param-key]]
   [(rf/subscribe [:global/energy-needed])
    (rf/subscribe [:global/energy-sources])])
 enrich-data-for-indicator)





(reg-sub
 :deriv/co2-per-kwh-mix
 (fn [_ _]
   [(rf/subscribe [:deriv/data-for-indicator :co2])
    (rf/subscribe [:global/energy-needed])])
 (fn [[{:keys [param-total]} energy-needed] _] ; param total are the CO2-Emissions in kt
   (if (and param-total energy-needed)
    (-> param-total                            ; kt/needed-nrg
        (/ energy-needed)                      ; kt/TWh
        ;; (/ 1000000000) ; kt/kWh
        ;; (* 1000000000) ; g/kWh
        )
    )))


(reg-sub
 :deriv/max-co-per-kwh
 (fn [_ _]
   (rf/subscribe [:global/energy-sources]))
 (fn [nrgs _]
   (apply max (map :co2 (vals nrgs)))))


;; (reg-sub
;;  :share/fossil-share
;;  (fn [_ _]
;;    (rf/subscribe [:global/energy-sources]))
;;  (fn [nrgs _]
;;    (reduce
;;     (fn [sofar {:keys [share]}]
;;       (+ sofar share)) 0
;;     (filter :fossil? (vals nrgs)))))




(reg-sub
 :ui/decab-color
 (fn [_ _]
   [(rf/subscribe [:deriv/max-co-per-kwh])
    (rf/subscribe [:deriv/co2-per-kwh-mix])])
 (fn [[max-co2-intensity actual-co2-intensity]  _]
   (if (and max-co2-intensity actual-co2-intensity)
    (let [bg (color/share-to-color
              max-co2-intensity
              actual-co2-intensity color/co2-gradients)]
      [(:col bg)
       (color/contrasty-bw bg)]
      ))))
 
(comment
  @(rf/subscribe [:deriv/max-co-per-kwh])
  @(rf/subscribe [:deriv/data-for-indicator :co2])
  @(rf/subscribe [:deriv/co2-per-kwh-mix])
  )


;; ##############
;; ### Panels ###
;; ##############

(rf/reg-event-db
 :ui/toggle-panel
 (fn [db [_ panel-key]]
   (update-in db [:ui :panels panel-key] not)))

(rf/reg-event-db 
 :ui/set-panel-visibility
 (fn [db [_ panel-key open?]]
   (assoc-in db [:ui :panels panel-key] open?)))

 
(rf/reg-event-db
 :ui/scroll-to-explanation
 (fn [db [_ exp-key]]
   (rf/dispatch [:ui/set-panel-visibility :explanations true])
   (.setTimeout js/window
    #(.scrollIntoView
      (.getElementById js/document (str "explanation-" (name exp-key)))) 50)
   db))

(reg-sub
 :ui/panel-open?
 (fn [db [_ panel-key]]
   (get-in db [:ui :panels panel-key])))


(comment
  (rf/dispatch [:ui/toggle-panel :explanations])
  @(rf/subscribe [:ui/toggle-panel :explanations]))

 
;; ##############
;; ### Legacy ###
;; ############## 


(defn- absolute-x
  "key should be :co2 or :deaths"
  [key energy-needed nrg]
  (-> (:share nrg)
      (/ 100)            ;TODO: from const
      (* energy-needed)  ; TWh of this nrg
      (* (key nrg))))

(defn- add-absolutes
  ""
  [key abs-key energy-needed nrgs]
  (h/map-vals
   (fn [nrg]
     (assoc nrg abs-key
            (absolute-x key energy-needed nrg)))
   nrgs))

(defn- calc-total
  ""
  [abs-key abs-added]
  (reduce #(+ %1 (abs-key (second %2)))
          0 abs-added))

(defn- add-share-of-x
  ""
  [abs-key share-key total abs-added]
  (h/map-vals
          #(assoc % share-key
                  (-> (abs-key %)
                      (/ total)
                      (* 100)
                      (h/nan->0))) ;TODO: from const
          abs-added))
  



(reg-sub
 ; attr key should be :co2 or :deaths
 :deriv/data-for-indicator-old
 (fn [[_ param-key]]
   [(rf/subscribe [:global/energy-needed])
    (rf/subscribe [:global/energy-sources])])
 (fn [[energy-needed energy-sources] [_ param-key]]
  (let [abs-key :absolute
        share-key :param-share
        abs-added (add-absolutes
                   param-key abs-key energy-needed energy-sources)
      total (calc-total abs-key abs-added)
      shares-added (add-share-of-x abs-key share-key
                              total abs-added)]
    {:param-total total
     :unit (get-in const/parameter-map [param-key :abs-unit])
     :energy-sources shares-added})))
 
