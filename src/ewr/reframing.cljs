(ns ewr.reframing
  (:require
   [cemerick.url :as url :refer (url url-encode)]
   [clojure.edn :as edn]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [ewr.color :as color]
   [ewr.config :as cfg]
   [ewr.constants :as constants]
   [ewr.constants :as const]
   ewr.csv
   [ewr.helpers :as h]
   [ewr.parameters :as params]
   [ewr.publications :as pubs]
   [ewr.remix :as remix]
   [ewr.serialization :as serialize]
   [re-frame.core :as rf :refer [reg-event-db reg-sub]]
   [reagent.core :as r]
   [reagent.ratom :as ratom]
   [thi.ng.color.core :as col]
   [troglotit.re-frame.debounce-fx]
   [vimsical.re-frame.cofx.inject :as inject]
   [vimsical.re-frame.fx.track :as fx.track]))

;; ###################
;; #### Technical ####
;; ###################

(rf/reg-cofx :tech/now
             (fn [cofx _]
               (assoc cofx :now (.now js/Date))))

(rf/reg-fx
 :tech/dispatches
 ;; Dispatches Events passed
 (fn [events]
   (doseq [event (remove nil? events)]
     (rf/dispatch event))))

(rf/reg-fx
 :tech/alert
 (fn [message]
   (when message
     (js/alert (str message)))))

(rf/reg-event-fx
 :tech/log
 (fn [_ [_ & prstrs]]
   (apply js/console.log prstrs)))

(reg-sub :tech/db
         (fn [db _] db))

(rf/reg-fx :clipboard/copy-to
           (fn [val]
             (let [el (js/document.createElement "textarea")]
               (set! (.-value el) val)
               (.setAttribute el "readonly" "")
               (set! (.-style el) "position: absolute; left: -9999px;")
               (.appendChild js/document.body el)
               (.select el)
               (js/document.execCommand "copy")
               (js/console.log "copied to clipboard: " val))))

(rf/reg-sub :summed-shares
            (fn [db _]
              (remix/sum-shares (:energy-sources db))))

;; ############################
;; ###### Input handling ######
;; ############################

;; These are used for the direct Inputs of Parameters
;; as defined in the ewr.parameters namespace

(reg-event-db
 :param/set
 (fn [db [_ param-key path parsed-newval]]
   (assoc-in db path (params/granularize param-key parsed-newval))))

(reg-sub :param/get
         (fn [db [_ path]]
           (h/nan->nil (get-in db path))))

;; ###########################
;; ###### Energy Needed ######
;; ###########################

(reg-sub
 :energy-needed/get
 (fn [db _]
   (h/nan->nil
    (remix/sum-shares (:energy-sources db)))))

(defn set-energy-needed [db newval]
  (let [current-share-sum  (remix/sum-shares (:energy-sources db))
        delta  (- newval current-share-sum)
        remixed-energies  (remix/distribute-energy delta (:energy-sources db))]
    (-> db
        (assoc :energy-sources remixed-energies))))

(reg-event-db
 :energy-needed/set
 (fn [db [_ newval]]
   (set-energy-needed db  (* constants/granularity-factor
                             (js/parseInt newval)))))

;; #####################################################
;; ########### Energy Sources and Parameters ###########
;; #####################################################

(reg-sub
 :nrg/keys
 (fn [db]
   (keys (:energy-sources db))))

(reg-sub
 :nrg/get-all
 ;; returns alls energy sources
 ;; combining variable values with constant ones
 (fn [db]
   (reduce
    (fn [sofar [nrg-key nrg-state]]
      (assoc sofar nrg-key
             (merge (get cfg/nrgs nrg-key) nrg-state)))
    {}
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
   (params/ungranularize :arealess-capacity
                         (if (> twh-share arealess-capacity)
                           arealess-capacity twh-share))))

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

(rf/reg-sub :pubs/loaded
            (fn [db _]
              (get-in db [:ui :loaded-pubs])))

(reg-sub
 :pub/nrg-param-loaded
 ;; returns the publication currently loaded for
 ;; a combination of Energy-source and Parameter
 ;; returns the entire map
 (fn [db [_ nrg-key param-key]]
   (let [curval        (get-in db [:energy-sources nrg-key param-key])
         matching-pubs (pubs/matching-pubs nrg-key param-key curval)
         last-loaded   (get-in db [:ui :loaded-pubs nrg-key param-key])]
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
                (*
                 (or (params/lookup-property param-key :granularity-factor) 1)
                 (get-in pub [:energy-sources nrg-key param-key])))
      (assoc-in [:ui :loaded-pubs nrg-key param-key]
                (:id pub))))

(defn load-nrg-pub
  "loads whatever value a given publication provides
 for a specific combination of energy-source-and parameter"
  [db nrg-key param-key pub]
  (when (not= pub nil) ;; if there actually is a publication
    (param-pub-into-db db pub nrg-key param-key)))

(reg-event-db
 :pub/load-nrg-param
 (fn [db [_ nrg-key param-key pub]]
   (load-nrg-pub db nrg-key param-key pub)))

(defn load-energy-needed-pub [db {:keys [id energy-needed] :as pub}]
  (if-not pub
    db
    (-> db
        (set-energy-needed (* constants/granularity-factor energy-needed))
        (assoc-in [:ui :loaded-pubs :energy-needed]
                  id))))

(reg-event-db
 :pub/load-energy-needed
 (fn [db [_ pub]]
   (load-energy-needed-pub db pub)))

(reg-sub
 :pub/loaded-energy-needed
 ;; the publication currently loaded for :energy-needed
 ;; Returns the whole map, not just the id
 (fn [_]
   [(rf/subscribe [:energy-needed/get])
    (rf/subscribe [:pubs/loaded])])
 (fn [[energy-needed loaded-pubs] _]
   (let [matching-pubs (pubs/matching-pubs-for-path [:energy-needed] (/ energy-needed constants/granularity-factor))
         last-loaded   (get loaded-pubs :energy-needed)]
     (return-loaded-pub matching-pubs last-loaded))))

(defn load-default-pubs [db]
  (-> (reduce
       (fn [db [_ nrg-key param-key]]
         (load-nrg-pub db  nrg-key param-key (pubs/default-pub nrg-key param-key)))
       db
       (for [nrg-key   (keys (get db :energy-sources)) ; … for alle combinations
             param-key params/common-param-keys] ; of energy-sources and parameters
         [nil nrg-key param-key]))
      (load-energy-needed-pub (first (pubs/pubs-for-global-value :energy-needed)))
      (load-nrg-pub :solar :arealess-capacity ; …for rooftop solar
                    (pubs/default-pub :solar :arealess-capacity))
      (load-nrg-pub :wind :arealess-capacity ; …for offshore wind
                    (pubs/default-pub :wind :arealess-capacity))
      (load-nrg-pub :hydro :cap         ; …for minors cap
                    (pubs/default-pub :hydro :cap))))

(def default-db
  (-> cfg/latest-preset
      ;; we load the default pubs only so the pub dropdowns show the pubs, corresponding
      ;; to values from the savestate…
      load-default-pubs
      ;; then we ensure that the savestate values are used
      ;; even if there are no correspondig pubs
      #_(merge cfg/latest-preset))) ;; comment this out to create a new default savestate

(rf/reg-event-db
 :pub/load-defaults
 ;; Used on initialization. Loads all default publications…
 (fn [db _] (load-default-pubs db)))

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

;; (reg-sub
;;  :nrg-share/get-relative-share
;;  ;; Returns the relative share of an Energy Source
;;  ;; in the total energy needed (in percent)
;;  (fn [db [_ nrg-key]]
;;    (get-in db [:energy-sources nrg-key :share])))

(reg-sub
 :nrg-share/get-absolute-share
 ;; Returns the absolute share of an Energy Source
 ;; in the total energy needed (in percent)
 (fn [db [_ nrg-key]]
   (get-in db [:energy-sources nrg-key :share])))

(reg-sub
 :nrg-share/get-absolute-share-ungranular
 ;; Returns the relative share of an Energy Source
 ;; in the total energy needed (in percent)
 (fn [[_ nrg-key]]
   (rf/subscribe [:nrg-share/get-absolute-share nrg-key]))
 (fn [share _]
   (/ share constants/granularity-factor)))

(reg-sub
 :nrg-share/get-relative-share
 ;; Returns the absolute share of an Energy Source
 ;; in the total energy needed (in TWh)
 (fn [[_ nrg-key]]
   [(rf/subscribe [:energy-needed/get])
    (rf/subscribe [:nrg-share/get-absolute-share nrg-key])])
 (fn [[energy-needed share] [_ nrg-key]]
   (-> share
       (* 1.0)
       (/ energy-needed))))

#_(reg-sub
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

;; ######################################
;; ######## Capping and Remixing ########
;; ######################################

(defn remove-cap-bump-notes [db]
  (reduce
   (fn [db key]
     (assoc-in  db [:energy-sources key :cap-bumped] false))
   db
   (keys (:energy-sources db))))

(defn note-bumped-into-cap [db nrg-key newval]
  (let [cap (get-in db [:energy-sources nrg-key :cap])]
    (cond-> db
      (> newval cap) (assoc-in [:energy-sources nrg-key :cap-bumped] true))))

(reg-event-db
 :nrg/remix-shares
 ;; Dispatched whenever the slider
 ;; associated with a energy-key is moved
 (fn [db [_ nrg-key newval]]
   (-> db
       remove-cap-bump-notes
       (note-bumped-into-cap nrg-key newval)
       (update :energy-sources
               #(remix/attempt-remix
                 nrg-key
                 newval
                 %))))) ;TODO: get parse-fn from config

(rf/reg-event-db :cap/set
                 (fn [db [_ nrg-key newval]]
                   (let [granularized-newval (* constants/granularity-factor newval)
                         remix-needed? (< granularized-newval (get-in db [:energy-sources nrg-key :cap]))]
                     (cond-> db
                       true (assoc-in [:energy-sources nrg-key :cap] granularized-newval)
                       remix-needed? (update :energy-sources
                                             #(remix/attempt-remix
                                               nrg-key
                                               granularized-newval
                                               %))))))

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
   [(rf/subscribe [:nrg/get nrg-key])
    (rf/subscribe [:nrg-share/get-absolute-share nrg-key])])
 (fn [[nrg share] [_ _nrg-key]]
   (let [{:keys [power-density] :as nrg}
         nrg
         area
         (-> share
             (- (:arealess-capacity nrg 0))
             (/ constants/granularity-factor)
             (* 1000000000000)        ; share in Wh
             (/ const/hours-per-year) ; needed W
             (/ power-density)        ; needed m²
             (/ 1000000))             ; needed km²
         radius           (if (or (< area 0) ; area < 0 possible with arealess-capacity
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
  [energy-sources [_ param-key]]
  (let [abs-added    (h/map-vals
                      #(assoc % :absolute
                              (-> (:share %)
                                  (/ constants/granularity-factor)
                                  (* (param-key %))))
                      energy-sources)
        total        (reduce #(+ %1 (:absolute (second %2)))
                             0 abs-added)
        shares-added (h/map-vals
                      #(assoc % :param-share
                              (-> (:absolute %)
                                  (/ total)
                                  (* 100)
                                  (h/nan->0))) ;TODO: from const
                      abs-added)]
    {:param-total    total
     :factor         (get-in params/common-parameter-map [param-key :indicator-factor] 0)
     :formatter      (get-in params/common-parameter-map
                             [param-key :indicator-formatter] #(Math/round %))
     :unit           (get-in params/common-parameter-map [param-key :abs-unit])
     :energy-sources shares-added}))

(reg-sub ; param-key should be :co2 or :deaths
 :deriv/data-for-indicator
 (fn [[_ _param-key]]
   (rf/subscribe [:nrg/get-all]))
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
   (when (and param-total (> energy-needed 0))
     (-> param-total                          ; kt/needed-nrg
         (* constants/granularity-factor)
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
   (when (and max-co2-intensity actual-co2-intensity)
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

(rf/reg-cofx
 :global/url
 (fn [coeffects _]
   (assoc coeffects :url (url (.. js/window -location -href)))))

(rf/reg-fx
 ;; Currently unused, reloads page
 :global/set-url-query-param-and-reload
 (fn [[param value]]
   (let [current-url
         (url/url (.. js/window -location -href))
         new-url (assoc-in current-url
                           [:query (str param)] (str value))]
     (set! (.. js/window -location -href) (str new-url)))))

(rf/reg-fx
 :save/remove-savestate-from-url
 (fn []
   (let [current-url
         (url/url (.. js/window -location -href))
         new-url (update-in current-url
                            [:query] #(dissoc % "s"))]
     (-> js/window
         .-history
         (.replaceState nil nil new-url)))))

(reg-sub
 :global/url
 (fn [_]
   (url/url (.. js/window -location -href))))

(defn db->savestate [db]
  (update
   (select-keys db [:energy-sources])
   :energy-sources
   (fn [nrgs]
     (into {}
           (map (fn [[key vals]]
                  [key (dissoc vals :locked?)]) ;TODO: no more reason to remove the locked key
                nrgs)))))

(reg-sub
 :save/savestate
 (fn [db _]
   (db->savestate db)))

(rf/reg-fx
 :global/set-url-query-params
 (fn [query-map]
   (when (exists? js/window) ; only do this in the browser
     (let [current-url
           (url/url (.. js/window -location -href))
           new-url
           (reduce-kv
            (fn [sofar k v]
              (assoc-in sofar
                        [:query (name k)] v))
            current-url
            query-map)]
       (-> js/window
           .-history
           (.pushState nil nil new-url))))))

(rf/reg-event-fx
 :savestate/rewrite-url
 (fn [_ [_ new-savestate-string]]
   {:global/set-url-query-params {:savestate new-savestate-string
                                  :sv        "1"}}))

(rf/reg-event-fx
 :savestate/encode-string
 (fn [{db :db} [_ savestate]]
   (let [savestate-string
         (serialize/savestate->string savestate)]
     (if savestate-string
       {:db (assoc db :savestate-string savestate-string)
        :global/set-url-query-params {:s savestate-string}}
       {:db (dissoc db :savestate-string)
        :save/remove-savestate-from-url true}))))

(rf/reg-event-fx
 :savestate/on-change
 (fn [_ [_ savestate]]
   {:dispatch-debounce {:key   :on-savestate-change
                        :event [:savestate/encode-string savestate]
                        :delay 250}}))

(reg-sub
 :save/savestate-string
 (fn [db _]
   (get db :savestate-string)))

(reg-sub
 :save/analysed-url
 (fn []
   [(rf/subscribe [:global/url])
    (rf/subscribe [:save/savestate-string])])
 (fn [[analysed-url savestate-string]]
   (cond-> analysed-url
     savestate-string (assoc-in [:query "s"] savestate-string))))

(reg-sub
 :save/url-string
 (fn []
   (rf/subscribe [:save/analysed-url]))
 (fn [analysed-url]
   (str
    analysed-url)))

(reg-sub
 :save/preview-query-string
 (fn []
   (rf/subscribe [:save/savestate-string]))
 (fn [savestate-string]
   (if  savestate-string (str "?s=" savestate-string) "")))

(reg-sub
 :save/querystring
 (fn []
   (rf/subscribe [:save/url-string]))
 (fn []))

(reg-sub
 :save/csv-string
 (fn []
   (rf/subscribe [:save/savestate]))
 (fn [savestate _]
   (str
    "data:application/octet-stream,"
    (ewr.csv/savestate-to-csv savestate))))

(rf/reg-event-fx
 :save/load-savestate-from-url
 [(rf/inject-cofx :global/url)]
 (fn [{:keys [url db] :as cofx} []]
   (if-let [url-savestate-string
            (get-in url [:query "s"])]
     (let [savestate (try
                       (serialize/string->savestate
                        url-savestate-string)
                       (catch js/error e
                         nil))]
       (cond-> {:db         (if savestate
                              (merge db savestate)
                              (assoc-in db [:ui :savestate-load-failed?] true))}
         (empty? savestate)
         (assoc :tech/alert "Leider konnte der gespeicherte Energiemix nicht geladen werden\n\nRechner startet mit Standardmix")
         (empty? savestate)
         (assoc :dispatch [:global/initialize false])))
     {})))

(reg-sub
 :save/savestate-load-failed?
 (fn [db _]
   (get-in db [:ui :savestate-load-failed?])))

(reg-event-db
 :clipboard/show-message
 (fn [db [_ key]]
   (assoc-in db [:ui :clipboard] key)))

(rf/reg-event-fx
 :clipboard/show-message-temporarily
 (fn [{:keys [db]} [_ key]]
   {:dispatch [:clipboard/show-message key]}))

(rf/reg-event-db
 :ui/hide-alert
 (fn [db [_ key]]
   (assoc-in db [:ui :copy-alert :show?] false)))

(rf/reg-event-fx
 :ui/set-copy-alert
 (fn [{:keys [db] :as _cofx} [_ text]]
   {:db                (-> db
                           (assoc-in [:ui :copy-alert :text] text)
                           (assoc-in [:ui :copy-alert :show?] true))
    :dispatch-debounce {:key   :remove-copy-alert
                        :event [:ui/hide-alert :copy-alert]
                        :delay 2000}}))

(rf/reg-event-fx
 :save/copy-link-to-clipboard
 [(rf/inject-cofx ::inject/sub [:save/url-string])]
 (fn [{:keys [:save/url-string db] :as _cofx} _]
   {:clipboard/copy-to url-string
    :dispatch          [:ui/set-copy-alert "Link zum Energiemix kopiert"]}))

(reg-sub :save/preview-link
         (fn [_]
           (rf/subscribe [:save/preview-query-string]))
         (fn [query-string _]
           (str (get cfg/settings :preview-api)
                query-string)))

(rf/reg-event-fx
 :save/copy-preview-link-to-clipboard
 [(rf/inject-cofx ::inject/sub [:save/preview-link])]
 (fn [{:keys [:save/preview-link db] :as _cofx} _]
   {:clipboard/copy-to preview-link
    :dispatch          [:ui/set-copy-alert "Link zum Vorschaubild kopiert"]}))

(reg-sub
 :ui/copy-alert
 (fn [db]
   (get-in db [:ui :copy-alert :text])))

(reg-sub
 :ui/copy-alert-visible?
 (fn [db]
   (get-in db [:ui :copy-alert :show?])))

;; ########################
;; ##### Global Stuff #####
;; ########################

(rf/reg-event-fx
 :global/initialize
 ;; initializes the db
 ;; loads the default publications
 (fn-traced [_ [_ load-savestate?]]
            (cond-> {:db              default-db
                     :tech/dispatches [(when (and (cfg/feature-active? :bookmark-state)
                                                  load-savestate?)
                                         [:save/load-savestate-from-url])]}
              (not load-savestate?) (assoc :save/remove-savestate-from-url true))))

(rf/reg-event-fx
 :global/register-tracks
 (fn [_ _]
   {::fx.track/register
    {:id :savestate-changed
     :subscription [:save/savestate]
     :event-fn (fn [savestate]  [:savestate/on-change savestate])}}))

(rf/reg-event-fx
 :global/dispose-tracks
 (fn [_ _]
   {::fx.track/dispose
    {:id :savestate-changed}}))
