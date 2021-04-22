(ns stromrechner.reframing
  (:require
   [re-frame.core :as rf :refer [reg-event-db reg-sub]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reagent.core :as r]
   [reagent.ratom :as ratom]
   [stromrechner.sources :as sources]
   [clojure.edn :as edn]
   [stromrechner.color :as color]
   [stromrechner.constants :as constants]
   [stromrechner.constants :as const]
   [stromrechner.parameters :as params]
   [stromrechner.helpers :as h]
   [stromrechner.config :as cfg]
   [stromrechner.remix :as remix]
   [thi.ng.color.core :as col]
   [wrap.compress :as compress]
   [cemerick.url :as url :refer (url url-encode)]
   [vimsical.re-frame.cofx.inject :as inject]))


;; ###################
;; #### Technical ####
;; ###################

(rf/reg-fx
 :tech/dispatches
 ;; Dispatches Events passed
 (fn [events]
   (doseq [event (remove nil? events)]     
     (rf/dispatch event))))

(rf/reg-event-fx
 :tech/log
 (fn [_ [_ & prstrs]]
   (apply js/console.log prstrs)))

(comment  
(rf/reg-event-fx
 :test/dsp
 (fn [{:keys [db]} [_]]
   {:tech/dispatches [[:tech/log "1"]
                      [:tech/log "2"]
                      [:tech/log "3"]]})))

(reg-sub :tech/db
         (fn [db _] db))


;; ########################
;; ##### Global Stuff #####
;; ########################

(def default-db
  {:energy-sources
   (get cfg/settings :init-mix)})

(rf/reg-event-fx
 :global/initialize
 ;; initializes the db
 ;; loads the default publications
 (fn-traced [_ _] 
            {:db default-db
             :tech/dispatches [[:global/load-default-pubs]
                               (when (cfg/feature-active? :bookmark-state)
                                 [:save/load-savestate-from-url])]}))

;; ############################
;; ###### Input handling ######
;; ############################

;; These are used for the direct Inputs of Parameters
;; as defined in the stromrechner.parameters namespace 

(reg-event-db
 :param/parse-and-set
 
 (fn [db [_ prepath param 
          unparsed-newval]]
   (let ; we take parse-fn from the parameter-definition
       [[param-key {:keys [parse-fn]}] param]
     (assoc-in db (conj prepath param-key)
               (parse-fn unparsed-newval)))))

(reg-sub :param/get
         (fn [db [_ pre-path param-key]]           
           (h/nan->nil (get-in db (conj pre-path param-key)))))


;; ###########################
;; ###### Energy Needed ######
;; ###########################

(reg-sub
 :energy-needed/get
 (fn [db _]
   (h/nan->nil
    (get db :energy-needed))))

(reg-event-db
 :energy-needed/set
 (fn [db [_ newval]]
   (assoc db :energy-needed newval)))


;; #####################################################
;; ########### Energy Sources and Parameters ###########
;; #####################################################

(def nrg-constants
  ;; stuff about energy-settings that never change
  ;; but are useful in many subscriptions…
  (get cfg/settings :nrg-constants))

(reg-sub
 :nrg/get-all
 ;; returns alls energy sources
 ;; combining variable values with constant ones
 (fn [db]
   (merge-with merge
               nrg-constants
               (:energy-sources db))))

(reg-sub
 :nrg/get 
 ;; get the current data for energy source
 ;; identified by nrg-key
 (fn [_]
   (rf/subscribe [:nrg/get-all])) 
 (fn [nrgs [_ nrg-key]]
   (get nrgs nrg-key)))

(reg-sub
 :nrg/get-param
 ;; get the value of one parameter
 ;; for one energy source 
 (fn [_]
   (rf/subscribe [:nrg/get-all]))
 (fn [nrgs [_ nrg-key param]]
   (get-in nrgs [nrg-key param])))

(reg-sub
 :nrg/exhausted-arealess
 ;; How much of the arealess capacity of the nrg
 ;; has been exhausted (offshore-wind and rooftop-solar)
 (fn [[_ nrg-key]]
   [(rf/subscribe [:nrg/get-param nrg-key :arealess-capacity])
    (rf/subscribe [:nrg-share/get-absolute-share nrg-key])])
 (fn [[arealess-capacity twh-share] [_ nrg-key]]
   (if (> twh-share arealess-capacity)
     arealess-capacity twh-share)))


;; ########################
;; ##### Publications #####
;; ########################

;;;;;
;;;;; Return currently loaded publication
;;;;;

(defn- return-loaded-pub 
  "Multiple pubs can provide identical values for
  the same parameter – they all match.
  We still want to find the one loaded by the user"
  [matching-pubs last-loaded]
  (case (count matching-pubs)
       0 nil
       1 (first matching-pubs)
        ; in case there is more than one pub with identical values
       (first (filter #(= (:id %) last-loaded)
                      matching-pubs))))

(reg-sub
 :energy-needed/loaded-pub
 ;; the publication currently loaded for :energy-needed
 ;; Returns the whole map, not just the id
 (fn [db _]
   (let [curval (get db :energy-needed)
         matching-pubs (sources/matching-pubs-for-path [:energy-needed] curval)
         last-loaded (get-in db [:ui :loaded-pubs :energy-needed])]
     (return-loaded-pub matching-pubs last-loaded))))

(reg-sub
 :nrg/loaded-pub
 ;; returns the publication currently loaded for
 ;; a combination of Energy-source and Parameter
 ;; returns the entire map
 (fn [db [_ nrg-key param-key]]
   (let [curval (get-in db [:energy-sources nrg-key param-key])
         matching-pubs (sources/matching-pubs nrg-key param-key curval)
         last-loaded (get-in db [:ui :loaded-pubs nrg-key param-key])]
     (return-loaded-pub matching-pubs last-loaded))))

;;;;;
;;;;; Load a publication
;;;;;

(defn param-pub-into-db  
  "Adds the data from a publication
  for a regular parameter into the db."
  ;; this can be useful in many handlers,
  ;; so it is separated out here
  [db pub nrg-key param-key]
  (-> db
      (assoc-in [:energy-sources nrg-key param-key]
                (get-in pub [:energy-sources nrg-key param-key]))
      (assoc-in [:ui :loaded-pubs nrg-key param-key]
                (:id pub))))

(reg-event-db
 ;; loads whatever value a given publication provides
 ;; for a specific combination of energy-source-and parameter
 :nrg/load-pub
 (fn [db [_ nrg-key param-key pub]]
   (if (not= pub nil) ;; if there actually is a publication
     (param-pub-into-db db pub nrg-key param-key))))

(reg-event-db
 :energy-needed/load-pub
 (fn [db [_ pub]]
   (if (not= pub nil)
     (-> db
         (assoc :energy-needed
                (get pub :energy-needed))
         (assoc-in [:ui :loaded-pubs :energy-needed]
                   (:id pub))))))


(rf/reg-event-fx
 :global/load-default-pubs
 ;; Used on initialization. Loads all default publications…
 (fn [_ _]
   {:tech/dispatches (into
                      [[:energy-needed/load-pub ; …for power needed
                        (first (sources/pubs-for-needed-power))]
                       [:nrg/load-pub :solar :arealess-capacity ; …for rooftop solar
                        (sources/default-pub :solar :arealess-capacity)]
                       [:nrg/load-pub :wind :arealess-capacity ; …for offshore wind
                        (sources/default-pub :wind :arealess-capacity)]]
                      
                      (for [nrg-key (map first (:energy-sources default-db)) ; … for alle combinations
                            param-key (map first params/common-nrg-parameters)] ; of energy-sources and parameters
                        [:nrg/load-pub nrg-key param-key
                         (sources/default-pub nrg-key param-key)]))}))

;; ###########################
;; ###### Energy shares ######
;; ###########################

(reg-sub
 :nrg-share/locked?
 (fn [db [_ nrg-key]]
   (if (get-in db [:energy-sources nrg-key :locked?])
     true false)))

(reg-event-db
 :nrg-share/toggle-lock
 (fn [db [_ nrg-key]]
   (update-in db [:energy-sources nrg-key :locked?] not)))

(reg-sub
 :nrg-share/get-relative-share
 ;; Returns the relative share of an Energy Source
 ;; in the total energy needed (in percent)
 (fn [db [_ nrg-key]]
   (get-in db [:energy-sources nrg-key :share])))

(reg-sub
 :nrg-share/get-absolute-share
 ;; Returns the absolute share of an Energy Source
 ;; in the total energy needed (in TWh)
 (fn [[_ nrg-key]]
   [(rf/subscribe [:energy-needed/get])
    (rf/subscribe [:nrg-share/get-relative-share nrg-key])]) 
 (fn [[energy-needed share] [_ nrg-key]]
   (-> share
       (/ 100)
       (* energy-needed))))

(reg-event-db
 :nrg/remix-shares
 ;; Dispatched whenever the slider
 ;; associated with a energy-key is moved
 (fn [db [_ nrg-key newval]]
   (update db :energy-sources
           #(remix/attempt-remix
             nrg-key (* 1 (js/parseInt newval)) %))))


;; ############################
;; ###### Derived-values ######
;; ############################

;;;;;
;;;;; for the map
;;;;;

(reg-sub
 :deriv/data-for-map
 ;; Subscription for the map view
 ;; Adds everything needed to draw the circles
 (fn [[_ nrg-key]]
   [(rf/subscribe [:energy-needed/get])
    (rf/subscribe [:nrg/get nrg-key])])
 (fn [[energy-needed nrg] [_ nrg-key]]
   (let [{:keys [share power-density props
                 capacity-factor deaths] :as nrg} nrg
         area (-> energy-needed
                     (* share)
                     (/ 100) ; share in TWh ;TODO: from constant
                     (- (:arealess-capacity nrg 0))
                     (* 1000000000000) ; share in Wh
                     (/ const/hours-per-year) ; needed W
                     ;; (/ capacity-factor) ; needed brute W                                        
                     (/ power-density) ; needed m²
                     (/ 1000000)) ; needed km²
         radius (if (or (< area 0) ; area < 0 possible with arealess-capacity
                     (js/isNaN area)) 0
                    (h/radius-from-area-circle area))]
     (assoc nrg
            :area area
            :relative-area (/ area cfg/total-landmass)
            :radius radius
            :diameter (* 2 radius)))))

;;;;;
;;;;; For the horizontal indicator bars
;;;;;

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
     :factor (get-in params/parameter-map [param-key :indicator-factor] 0)
     :formatter (get-in params/parameter-map
                        [param-key :indicator-formatter] #(Math/round %))
     :unit  (get-in params/parameter-map [param-key :abs-unit])
     :energy-sources shares-added}))

(reg-sub ; param-key should be :co2 or :deaths
 :deriv/data-for-indicator
 (fn [[_ param-key]]
   [(rf/subscribe [:energy-needed/get])
    (rf/subscribe [:nrg/get-all])])
 enrich-data-for-indicator)

;;;;;
;;;;; For the CO2-intensity indicator
;;;;;

(reg-sub
 :deriv/co2-per-kwh-mix
 ;; returns the current co2-intensity in g/kWh
 (fn [_ _]
   [(rf/subscribe [:deriv/data-for-indicator :co2])
    (rf/subscribe [:energy-needed/get])])
 (fn [[{:keys [param-total]} energy-needed] _] ; param total are the CO2-Emissions in kt
   (if (and param-total (> energy-needed 0))
    (-> param-total                            ; kt/needed-nrg
        (/ energy-needed)))))  ; g/kWh


(reg-sub
 :deriv/max-co-per-kwh
 ;; Returns the max CO2-density
 ;; among all energy sources
 ;; this should always be coal,
 ;; but let's do it properly anyway
 (fn [_ _]
   (rf/subscribe [:nrg/get-all]))
 (fn [nrgs _]
   (apply max (map :co2 (vals nrgs)))))

(reg-sub
 :ui/decab-color
 ;; returns the color for the heading
 ;; indicating CO2-intensity
 (fn [_ _]
   [(rf/subscribe [:deriv/max-co-per-kwh])
    (rf/subscribe [:deriv/co2-per-kwh-mix])])
 (fn [[max-co2-intensity actual-co2-intensity]  _]
   (if (and max-co2-intensity actual-co2-intensity)
    (let [bg (color/share-to-color
              max-co2-intensity
              actual-co2-intensity cfg/co2-colors)]
      [(:col bg)
       (color/contrasty-bw bg)])))) 


;; ############################
;; ###### User Interface ######
;; ############################

;; Opening Panels and scrolling to elements

(rf/reg-event-db
 :ui/toggle-panel-visibility
 ;; toggles the “open” status of a collapsible panel
 (fn [db [_ panel-key]]
   (update-in db [:ui :panels panel-key] not)))

(rf/reg-event-db 
 :ui/set-panel-visibility
 ;; sets the “open” status of a collapsible panel
 (fn [db [_ panel-key open?]]
   (assoc-in db [:ui :panels panel-key] open?)))

(rf/reg-fx
 :ui/scroll-to-id
 ;; Scrolls to the DOM element with a particular id
 (fn [[id timeout]]
   (.setTimeout js/window
    #(.scrollIntoView
      (.getElementById js/document id)) timeout)))

(rf/reg-event-fx
 :ui/scroll-to-explanation
 ;; Makes visible the panel containing the explanation
 ;; identified by EXP-KEY. Then scrolls to it.
 (fn [db [_ exp-key]]
   {:tech/dispatches [[:ui/set-panel-visibility :explanations true]]
    :ui/scroll-to-id [(str "explanation-" (name exp-key))
                               50]})) ;; ← too hacky. How can I properly wait vor visibility

(reg-sub
 :ui/panel-open?
 (fn [db [_ panel-key]]
   (get-in db [:ui :panels panel-key])))


;; ##############
;; ### Saving ###
;; ##############

;; Functions related to storing the current state
;; in the URL ; still experimental

(rf/reg-cofx
 :global/url
 (fn [coeffects _]
   (assoc coeffects :url (url (.. js/window -location -href)))))

(rf/reg-fx
 ;; Currently unused
 :global/set-url-query-param
 (fn [[param value]]
   (let [current-url
         (url/url (.. js/window -location -href))
         new-url (assoc-in current-url
                           [:query (str param)] (str value))]
     (js/console.log (str new-url))
     (set! (.. js/window -location -href) (str new-url)))))

(reg-sub
 :global/url
 (fn [_]
   (url/url (.. js/window -location -href))))

(reg-sub
 :save/savestate
 (fn [db _]
   (select-keys db [:energy-sources :energy-needed])))

(reg-sub
 :save/savestate-string
 (fn []
   (rf/subscribe [:save/savestate]))
 (fn [savestate _]
   (compress/compress-b64(str savestate))))

(reg-sub
 :save/url-string
 (fn []
   [(rf/subscribe [:global/url])
    (rf/subscribe [:save/savestate-string])])
 (fn [[analysed-url savestate-string]]
   (str
    (assoc-in analysed-url
              [:query "savestate"]
              savestate-string))))


(rf/reg-event-fx
 :save/load-savestate-from-url
 [(rf/inject-cofx :global/url)]
 (fn [{:keys [url db]:as cofx} []]
   (if-let [url-savestate-string
            (get-in url [:query "savestate"])]
     (let [savestate (edn/read-string
                      (compress/decompress-b64
                       url-savestate-string))]
       (js/console.log savestate)
       {:db (merge db savestate)})
     {})))