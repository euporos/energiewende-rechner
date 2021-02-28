(ns stromrechner.views
  (:require
   [stromrechner.constants :as constants]
   [re-frame.core :as rf]
   [stromrechner.sources :as sources]
   [clojure.string :as str]
   [stromrechner.icons :as icons  :refer [icon]]
   [clojure.edn :as edn]
   [stromrechner.constants :as const]
   [stromrechner.helpers :as h]
   [stromrechner.config :as cfg]))

;; ########################
;; ##### Common Stuff #####
;; ########################

;;
;; Panels
;;

(defn panel
  ""
  [heading & comps]
  [:nav.panel
   [:div.panel-heading heading]
   (into [:div.pt-3.pb-3.pr-3.pl-3] 
           comps)])

(defn collapsible-panel
  ""
  [heading & comps]
  [:nav 
     {:class "panel"} 
     [:details 
      [:summary 
       {:class "panel-heading"} heading] 
      (into [:div.block.pt-3.pb-3.pr-3.pl-3 ] comps)]])

;;
;; Inputs
;;

(defn param-input
  ""
  [pre-path param]
  (let [[param-key {:keys [unit input-attrs]}] param ]
    [:div.field.is-horizontal  
     [:div.field-body 
      [:div.field
       {:style {:width "7.5rem"}}
       [:p.control.is-expanded.has-icons-right
        [:input.input         
         (merge input-attrs
                {:value @(rf/subscribe [:param/get pre-path param-key])
                 :on-change (fn [eventobj]
                              (.preventDefault eventobj)
                              (rf/dispatch [:param/set-unparsed
                                             pre-path param
                                            (-> eventobj
                                                .-target
                                                .-value)]))})] 
        [:span.icon.is-small.is-right
         {:style {:margin-right "1rem"}}
         unit]]]]]))

(defn publication-dropdown
  ""
  [{:keys [value-subscription publications partial-dispatch]}]
  [:div.field.is-horizontal
   [:div.field-body
    [:div.field 
     [:p.control.is-expanded
      [:select.input
       {;:name (str/join ["pub" nrg-key param])
        :value (str value-subscription)
        :on-change #(let [newval (-> % .-target .-value)]
                      (.preventDefault %)
                      (rf/dispatch (conj partial-dispatch
                                         (edn/read-string newval))))}
       [:option {:value nil}  "Benutzerdefiniert"]
       (for [pub publications ]
         ^{:key (:id pub)}           
         [:option {:value (str pub)}
          (:id pub)])]]]]])

;; #####################################
;; ######## Solar-Roof-capacity ########
;; #####################################

(defn solar-roof-capacity-dropdown
  ""
  []
  [publication-dropdown
   {:value-subscription @(rf/subscribe [:pub/loaded :solar :arealess-capacity])
    :partial-dispatch [:pub/load :solar :arealess-capacity]
    :publications (sources/pubs-for-param :solar :arealess-capacity)}])

(defn solar-roof-capacity-input
  ""
  []
  [param-input [:energy-sources :solar] const/arealess-capacity])


(defn solar-roof-capacity
  ""
  []
  (panel [:span "Solarkapazität Dächer";; (icons/icon2 "#999999" icons/sun)
          (if-let [href (:link @(rf/subscribe [:pub/loaded :solar :arealess-capacity]))]
            [:a {:target "_blank"
                 :href href} "→ Quelle"])]
         [:div.block
          [:div.mb-1
           [solar-roof-capacity-dropdown]]
          [:div
           [solar-roof-capacity-input]]]))
 

;; ########################
;; ##### Energy Needed ####
;; ######################## 

(defn energy-needed-dropdown
  ""
  []
  [publication-dropdown
   {:value-subscription @(rf/subscribe [:energy-needed/loaded])
    :partial-dispatch [:energy-needed/load]
    :publications (sources/pubs-for-needed-power)}])

 
(defn energy-needed
  ""
  []
  (panel [:span "Jährlicher Strombedarf";; (icons/icon2 "#999999" icons/sun)
          (if-let [href (:link @(rf/subscribe [:energy-needed/loaded]))]
            [:a {:target "_blank"
                 :href href} "→ Quelle"])]
         [:div.block
          [:div.mb-1
           [energy-needed-dropdown]]
          [:div
           [param-input [] const/energy-needed]]]))


;; ####################################################################
;; ############## Parameter-Inputs »Profi-Einstellungen« ##############
;; ####################################################################

(defn param-dropdown
  ""
  [nrg-key param]
  (let [[param-key _] param]
    [publication-dropdown
     {:value-subscription @(rf/subscribe [:pub/loaded nrg-key param-key])
      :partial-dispatch [:pub/load nrg-key param-key] ; the on-change-val gets conj'd onto this
      :publications (sources/pubs-for-param nrg-key param-key)}]))

(defn pub-link
  ""
  [nrg-key [param-key _]]
  (if-let [loaded-pub-link
           (:link @(rf/subscribe
                    [:pub/loaded nrg-key param-key]))]
    [:a {:href loaded-pub-link
         :target "_blank"
         :rel "noopener noreferrer"} " → Quelle"]))

(defn param-settings
  ""
  [nrg-key param]
  [:div.column
   {:key (str nrg-key (first param))}
   [:h3.title.is-5
    (:name (second param))
    [pub-link nrg-key param]]
   [:div.columns.is-mobile
    [:div.column
     [param-dropdown nrg-key param]]
    [:div.column.is-narrow    
     [param-input [:energy-sources nrg-key] param]]]])

(defn params-for-energy-source
  ""
  [[nrg-key nrg]]
  [:div.block
   [:span.title.is-4 (:name nrg)]
   [:div.columns
    (map (partial param-settings nrg-key)
         constants/parameters)]])

(defn detailed-settings []
  [:div#detailed-settings.pl-3.pr-3
   (collapsible-panel
    "Detaillierte Einstellungen"
    (for [nrg-source @(rf/subscribe [:global/energy-sources])]
      ^{:key (first nrg-source)}
      [params-for-energy-source nrg-source])

    [:span "Solarkapazität Dächer";; (icons/icon2 "#999999" icons/sun)
          (if-let [href (:link @(rf/subscribe [:pub/loaded :solar :arealess-capacity]))]
            [:a {:target "_blank"
                 :href href} "→ Quelle"])]
    [solar-roof-capacity-dropdown]
    [solar-roof-capacity-input])])


;; ######################
;; ##### Energy-Mix #####
;; ######################

(Math/round (* 10 (/ 100 3)))


(defn lock-icon
  ""
  [nrg-key]
  [:span
   {:on-click #(rf/dispatch [:nrg/toggle-lock nrg-key])}
   (icon (if @(rf/subscribe [:nrg/locked? nrg-key])
           icons/lock-filled icons/lock-open))])


(defn energy-slider [[nrg-key {:keys [name props share color]}]]
  [:div.eslider {:style {:background-color color
                         :width "100%"}}
   [lock-icon nrg-key]
   [:label
    ;; [:img {:src  (get-in cfg/settings [:nrg-constants nrg-key :icon])
    ;;        :style {:height "1rem"
    ;;                :padding-top "0.2rem"
    ;;                :margin-right "0.2rem"}}]
    [:strong name " "
     ;; (/ (Math/round (* 10 share)) 10)
     (Math/round share)"% | "
            (Math/round 
             @(rf/subscribe [:nrg-share/get-abs nrg-key]))            
            " TWh"]]
   [:input {:type "range"  :min 0 :max 100
            :style {:width "100%"}
            :value (str (/ share 1))
            :on-change #(let [newval (-> % .-target .-value)]
                          (.preventDefault %)
                          (rf/dispatch-sync
                           [:nrg-share/remix
                            nrg-key (* 1 (js/parseInt newval))]))}]])

(defn energy-mix
  "" 
  []
  (panel "Strommix"
         [:div.mb-3
          "Stelle hier den Strommix der Zukunft zusammen…"]
         (for [nrg-source @(rf/subscribe [:global/energy-sources])]
           ^{:key (str (first nrg-source))}
           [:div [energy-slider nrg-source]])))

;; #########
;; ## Map ##
;; #########


(defn circle-by-area
  ""
  [radius opts props]
  [:circle 
   (merge
    {:r (str radius) ; str avoids a NaN error
     :stroke "black" 
     :stroke-width "0"}
    props)])
 
(defn circle-energy
  ""
  [nrg-key] 
  (let [{:keys [props radius area color darker-color]}
        @(rf/subscribe [:deriv/surface-added nrg-key])
        text-x (:cx props)
        text-y (:cy props)]
    [:<>
     (circle-by-area
      radius {} props)
     (let [area (Math/round area)
           outside? (< radius 55)]
       

       (when (> area 0)
         
         [:text {:text-anchor "middle"
                 :zindex 1000
                 :alignment-baseline "middle"
                 :font-weight "bold"
                 :fill (if (< radius 5) darker-color)
                 :x text-x
                 :y (if outside?
                      (- text-y radius 10)
                      text-y)}
          
          (str (h/structure-int
                area) " km²")]))]))

(defn mapview
  ""
  []
  [:div.mapview
   (into [:svg.karte
          {:viewBox "0 0 640 876"
           ;:preserveAspectRatio true
           }]
         (doall (map circle-energy
                     @(rf/subscribe [:global/energy-keys]))))])



;; ######################
;; ##### Indicators #####
;; ######################


(defn indicator
  ""
  [heading param-key]
  (let [{:keys [param-total unit energy-sources]}
        @(rf/subscribe [:deriv/data-for-indicator param-key])]
   [:div.todesanzeige.mb-3
    [:div
     [:strong heading (str
                   (h/structure-int (Math/round param-total)) unit )]
     (into [:div ] (interpose " | "
                              (map (fn [{:keys [name absolute]}]
                                     [:span 
                                      name ": " (h/structure-int
                                                 (Math/round absolute)) unit ])
                                   (vals energy-sources))))]
    [:div
     (into [:svg 
            {:width "100%" 
             :height "2em"}]
           (second
            (reduce
             (fn [[left-marg sofar] {:keys [name param-share color]}]
               [(+ left-marg
                   param-share) (conj sofar
                                      [:rect 
                                       {:key name
                                        :x (str left-marg "%")
                                        :width (str param-share "%") 
                                        :height "2em" 
                                        :style {:fill color
                                                :stroke-width "0"
                                                :stroke "black"}}])])
             [0 []]
             (vals energy-sources))))]]))


;; ############################ 
;; ###### Main Component ######
;; ############################

(defn main-panel []
  [:div
   [:div.anwendung.pt-3.pb-3.pl-3.pr-3
    [:div.columns
     [:div.anzeige.column.is-two-thirds
      [mapview]]
     [:div.column
      [energy-mix]
      [energy-needed]
      [solar-roof-capacity]]]
    [indicator [:span "Jährliches CO" [:sub "2"] "-Äquivalent: " ]
     :co2]
    [indicator "Statistisch erwartbare Todesfälle pro Jahr: "
     :deaths]
    ]
   [detailed-settings]])
 
 
  
