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
   [stromrechner.config :as cfg]
   [stromrechner.text :as text]
   ))

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

;; (defn collapsible-panel
;;   ""
;;   [heading & comps]
;;   [:nav 
;;      {:class "panel"} 
;;      [:details 
;;       [:summary 
;;        {:class "panel-heading"} heading] 
;;       (into [:div.block.pt-3.pb-3.pr-3.pl-3 ] comps)]])


(defn collapsible-panel
  ""
  [heading & comps]
  [:nav
   {:class "panel"} 
     [:div
      [:div
       {:class "panel-heading"} heading] 
      (into [:div.block.pt-3.pb-3.pr-3.pl-3 ] comps)]])




(defn panel-toggler
  ""
  [open?]
  [:div.mr-1
   {:style {:display "inline-block"
            :transition "all .2s"
            :transform (if open? "rotate(90deg)" nil)}}
   "►"])



(defn controlled-panel
  ""
  [key heading & comps]
  (let [open? @(rf/subscribe [:ui/panel-open? key])]
    [:nav
     {:class "panel"}
     [:div
      [:div
       {:class "panel-heading"
        :style {:cursor "pointer"}
        :on-click (h/dispatch-on-x [:ui/toggle-panel key])}
       (panel-toggler open?)
       heading] 
      [:div
            {:style {:overflow "hidden"
                     :max-height (if (not open?) 0)
                     ;:transition "max-height 1s ease-out"
                     }}
       (into [:div.block.pt-3.pb-3.pr-3.pl-3 ] comps)]]]))

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
       [:p.control.is-expanded ;.has-icons-right
        [:input.input         
         (merge input-attrs
                {:value @(rf/subscribe [:param/get pre-path param-key])
                 :on-change (h/dispatch-on-x [:param/set-unparsed pre-path param])
                 ;; (fn [eventobj]
                 ;;              (.preventDefault eventobj)
                 ;;              (rf/dispatch [:param/set-unparsed
                 ;;                             pre-path param
                 ;;                            (-> eventobj
                 ;;                                .-target
                 ;;                                .-value)]))
                 })] 
        ;; [:span.icon.is-small.is-right
        ;;  {:style {:margin-right "1rem"}}
        ;;  unit]
        ]]]]))

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
  (panel [:span "Jährlicher Strombedarf in TWh";; (icons/icon2 "#999999" icons/sun)
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
  [nrg-key param-key]
  (if-let [loaded-pub-link
           (:link @(rf/subscribe
                    [:pub/loaded nrg-key param-key]))]
    [:a {:href loaded-pub-link
         :target "_blank"
         :rel "noopener noreferrer"} "→ Quelle"]))

(defn param-settings
  ""
  [nrg-key param]
  [:div.column
   {:key (str nrg-key (first param))}
   [:h3.title.is-5
    (:name (second param))
    [pub-link nrg-key (first param)]]
   [:div.columns.is-mobile
    [:div.column
     [param-dropdown nrg-key param]]
    [:div.column.is-narrow    
     [param-input [:energy-sources nrg-key] param]]]])



;; document.getElementById('id').scrollIntoView();



(get-in text/snippets [:wind :text])

(defn params-for-energy-source    
  ""
  [[nrg-key nrg]]
  [:div.block
   [:span.title.is-4 (:name nrg)]
   [:span.ttip.ml-1
    {:on-click (h/dispatch-on-x [:ui/scroll-to-explanation nrg-key])}
    ;; {:data-tooltip (get-in text/snippets [nrg-key :text])}
    "?"]
   [:div.columns
    (map (partial param-settings nrg-key)
         constants/parameters)]])

;; (defn quellenlink
;;   ""
;;   [nrg-key parameter]
;;   (if-let [href (:link @(rf/subscribe [:pub/loaded nrg-key parameter]))]
;;                     [:a {:target "_blank"
;;                          :href href} "→ Quelle"]))

(defn detailed-settings []
  [:div#detailed-settings.pl-3.pr-3
   (controlled-panel :details
    "Detaillierte Einstellungen"
    [:span "Solarkapazität Dächer in TWh";; (icons/icon2 "#999999" icons/sun)
     [pub-link :solar :arealess-capacity]]
    [solar-roof-capacity-dropdown]
    [solar-roof-capacity-input]
    (for [nrg-source @(rf/subscribe [:global/energy-sources])]
      ^{:key (first nrg-source)}
      [params-for-energy-source nrg-source]))])

;; ################################
;; ####### Tabular Settings #######
;; ################################



(defn param-settings-tabular
  ""
  [nrg-key param]
  [:div
   {:key (str nrg-key (first param))}
   [:div
    [:div.columns.is-vcentered.is-mobile
     [:div.column
      [param-input [:energy-sources nrg-key] param]]
     [:div.column [pub-link nrg-key (first param)]]]]
   [:div.mt-1 [param-dropdown nrg-key param]]])

(defn settings-table-row
  ""
  [[nrg-key nrg]]
  [:tr
   [:th.is-vcentered
    {:style {:cursor "help"}
     :on-click (h/dispatch-on-x [:ui/scroll-to-explanation nrg-key])}
    (:name nrg) [:a "*"]]
   (map-indexed
    (fn [i param]
      [:td {:key i} [param-settings-tabular
                     nrg-key param] ])
    const/parameters)])

(defn settings-table-top-row
  ""
  []
  [:tr [:th ]
       (map-indexed
        (fn [i [param-key param]]
          [:th.has-text-centered
           {:key i
            :style {:cursor "help"}
            :on-click (h/dispatch-on-x [:ui/scroll-to-explanation param-key])}
           (:name param)[:a "*"]])
        const/parameters)])

(defn detailed-settings-tabular []
  [:div#detailed-settings.is-hidden-touch.pl-3.pr-3.is-hidden-touch
   (controlled-panel :details
    "Detaillierte Einstellungen"          
    [:table.table
     {:style {:margin-left "auto"
              :margin-right "auto"}}
     [:thead
      [settings-table-top-row]]
     [:tbody
      (for [nrg-source @(rf/subscribe [:global/energy-sources])]
        ^{:key (first nrg-source)}
        [settings-table-row nrg-source])]]
    [:div
     {:style {:margin-left "auto"
              :margin-right "auto"}}
     [:span.has-text-weight-bold
      "Solarkapazität Dächer in TWh"]
     [:div.columns.is-mobile.is-vcentered      
      [:div.column [solar-roof-capacity-input]]
      [:div.column [solar-roof-capacity-dropdown]]
      [:div.column [pub-link :solar :arealess-capacity]]]])])


;; ########################
;; ##### Explanations #####
;; ########################


(defn param-settings-pair-explanations
  ""
  [nrg-key param]
  [:div.block
   {:key (str nrg-key (first param))}
   [:div.has-text-weight-bold.mb-1
    (:name (second param)) " "
    [pub-link nrg-key (first param)]]
   [:div.columns.is-mobile
    [:div.column
     [param-dropdown nrg-key param]]
    [:div.column.is-narrow    
     [param-input [:energy-sources nrg-key] param]]]])

(defn params-for-explanations
  ""
  [nrg-key nrg]
  (js/console.log "nrg is " nrg)
  [:div.is-hidden-desktop
   [:h5.title.is-5 "Parameter für " (:name nrg) ":"]
   (map
    (fn [param]
      [param-settings-pair-explanations nrg-key param])
    const/parameters)
   (when (= nrg-key :solar)
     [param-settings-pair-explanations
      nrg-key const/arealess-capacity])])

(defn format-snippet
  ""
  ([i exp-key]
   (format-snippet i exp-key nil))
  ([i exp-key supplement]
   (let [{:keys [heading text]}
         (get text/snippets exp-key)]
     [:div.block
      {:key i
       :id (str "explanation-" (name exp-key))}
      [:h4.title.is-4 heading]
      [:div.content
       (h/dangerous-html text)]
      supplement])))

(defn explanations
  ""
  []
  [:div#detailed-settings.pl-3.pr-3.mt-4
   [controlled-panel :explanations
    [:<> "Erläuterungen" [:span.is-hidden-desktop " und Parameter" ]]
    [:div.block
     [:h3.title.is-3 {:id "was-ist-das"}"Was ist das?"]
     (h/dangerous-html (get-in text/snippets [:general :text]))]
    [:div.block
     [:h3.title.is-3 "Energiequellen"]
     (map-indexed (fn [i [nrg-key nrg]]
                    (format-snippet
                     i nrg-key (params-for-explanations nrg-key nrg))) cfg/nrgs)]
    [:h3.title.is-3 "Parameter"]
    (map-indexed format-snippet const/param-keys)]])


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


(defn toggler
  ""
  [nrg-key]
  (let [id  (str "toggler-" (name nrg-key))]
    [:span.field
     [:input.switch.is-small.is-rounded
      {:id id
       :type "checkbox"
       :on-change #(rf/dispatch [:nrg/toggle-lock nrg-key])
       :checked (not @(rf/subscribe [:nrg/locked? nrg-key]))}] 
     [:label 
      {:for id} ;; [lock-icon nrg-key]
      ]]))




(defn energy-slider [[nrg-key {:keys [name props share color]}]]
  [:div.eslider.pt-1 {:style {:background-color color
                         :width "100%"}}
   [:span.ml-2 [toggler nrg-key]
    [lock-icon nrg-key]
    ]   
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
                 :alignment-baseline "central"
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
  (let [{:keys [param-total unit energy-sources formatter] :as nrg}
        @(rf/subscribe [:deriv/data-for-indicator param-key])
        unit (if unit (str " " unit))]
   [:div.todesanzeige.mb-3
    [:div
     [:strong heading (formatter param-total) unit]
     (into [:div ] (interpose " | "
                              (map (fn [{:keys [name absolute]}]
                                     [:span 
                                      name ": " (formatter absolute) unit ])
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
      ;; [solar-roof-capacity]
      ]]
    [indicator [:span "Jährliches CO" [:sub "2"] "-Äquivalent: " ]
     :co2]
    [indicator "Statistisch erwartbare Todesfälle pro Jahr: "
     :deaths]]
   [detailed-settings-tabular]
   [explanations]])
 
 
  
