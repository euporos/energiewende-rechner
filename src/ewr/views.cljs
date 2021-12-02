(ns ewr.views
  (:require
   [ewr.icons :as icons]
   [ewr.constants :as constants]
   [re-frame.core :as rf]
   [ewr.publications :as pubs]
   [clojure.string :as str]
   [clojure.edn :as edn]
   [ewr.parameters :as params]
   [ewr.helpers :as h]
   [ewr.config :as cfg :refer [snippet]]
   [reagent.core :as r]
   [md5.core :as md5])
  (:require-macros [ewr.macros :as m]))

;; ########################
;; ##### Common Stuff #####
;; ########################

(defn with-tooltip
  "Renders text with Bulma tooltip"
  ([text]
   (with-tooltip text "zur Erläuterung springen"))
  ([text tooltip]
   [:span {:data-tooltip tooltip} text [:a "*"]]))

;;
;; Panels
;;

(def co2
  [:<> "CO" [:sub "2"]])

(defn panel
  "Generic Bulma Panel"
  [heading & comps]
  [:nav.panel
   [:div.panel-heading heading]
   (into [:div.pt-3.pb-3.pr-3.pl-3]
         comps)])

(defn panel-toggler
  "indicates if Panel is open"
  [open?]
  [:div.mr-1
   {:style {:display    "inline-block"
            :transition "all .2s"
            :transform  (if open? "rotate(90deg)" nil)}}
   "►"])

(defn controlled-panel
  "Panel that can be opened and closed.
  KEY ist used to identify openness of
  this particular panel in the Re-Frame DB."
  [key heading & comps]
  (let [open? @(rf/subscribe [:ui/panel-open? key])]
    [:nav
     {:class "panel"}
     [:div
      [:div
       {:class    "panel-heading panel-heading--collapsible"
        :on-click (h/dispatch-on-x [:ui/toggle-panel-visibility key])}
       (panel-toggler open?)
       heading]
      [:div
       {:style {:overflow   (if open? "visible" "hidden")
                :max-height (if (not open?) 0)}}
       (into [:div.block.pt-3.pb-3.pr-3.pl-3] comps)]]]))

;;
;; Inputs
;;

(defn param-input
  "Input Element for a Parameter.
  PARAM has the form defined in  ewr.constants.
  Pre-path indicates where in the DB the parameter-dfn can be found."
  ;; I have tried to avoid this tight coupling with the DB
  ;; but solutions were unsatisfactory
  [pre-path parameter-dfn width show-unit?]
  (let [[param-key {:keys [unit input-attrs]}] parameter-dfn]
    [:div.field.is-horizontal
     [:div.field-body
      [:div.field
       {:style {:width width}}
       [:p.control.is-expanded
        [:input.input
         (merge input-attrs
                {:value     @(rf/subscribe [:param/get pre-path param-key])
                 :on-change (h/dispatch-on-x
                             [:param/parse-and-set pre-path parameter-dfn])})]]]]]))

(defn publication-dropdown
  [{:keys [value-subscription publications partial-event]}]
  [:div.field.is-horizontal
   [:div.field-body
    [:div.field
     [:p.control.is-expanded
      [:select.input
       {:value     (str value-subscription)
        :on-change (h/dispatch-on-x
                    false ; not synchronously
                    edn/read-string ; parse back into data structure
                    partial-event)} ; before dispatch
       [:option {:value nil}  "Benutzerdefiniert"]
       (for [pub publications]
         ^{:key (:id pub)}
         [:option {:value (str pub)} ; only strings can be values ;(
          (:id pub)])]]]]])

;; ############################
;; ###### Arealess Power ######
;; ############################

;; These inputs deal with the arealess power parameter,
;; which is special insofar as not all energy sources have it defined.
;; Currently there are only rooftop solar and offshore wind.

(defn arealess-dropdown
  "Dropdown to select a publication for
  the arealess capacity
  i.e. rooftop solar or offshore wind"
  [nrg-key]
  [publication-dropdown
   {:value-subscription @(rf/subscribe [:nrg/loaded-pub nrg-key :arealess-capacity])
    :partial-event      [:nrg/load-pub nrg-key :arealess-capacity]
    :publications       (pubs/pubs-for-param nrg-key :arealess-capacity)}])

(defn arealess-input
  "Input to enter a value for
  the arealess capacity,
  i.e. rooftop solar or offshore wind"
  [nrg-key]
  [param-input [:energy-sources nrg-key]
   params/arealess-capacity])

;; ########################
;; ##### Energy Needed ####
;; ########################

(defn energy-needed-dropdown
  []
  [publication-dropdown
   {:value-subscription @(rf/subscribe [:energy-needed/loaded-pub])
    :partial-event      [:energy-needed/load-pub]
    :publications       (pubs/pubs-for-needed-power)}])

(defn energy-needed
  []
  (panel [:span "Jährlicher Strombedarf in TWh"]
         [:div.block
          [:div.columns.is-mobile.is-vcentered.mb-0
           [:div.column
            [param-input [] params/energy-needed]]
           (if-let [href (:link @(rf/subscribe [:energy-needed/loaded-pub]))]
             [:div.column.is-narrow.has-text-centered
              [:a {:target "_blank"
                   :href   href} " → Quelle"]])]
          [:div
           [energy-needed-dropdown]]]))

;; ####################################################################
;; ############## Parameter-Inputs »Profi-Einstellungen« ##############
;; ####################################################################

;; These are for parameters as defined in ewr.constants

(defn param-dropdown
  ""
  [nrg-key parameter-dfn]
  (let [[param-key _] parameter-dfn]
    [publication-dropdown
     {:value-subscription @(rf/subscribe [:nrg/loaded-pub nrg-key param-key])
      :partial-event      [:nrg/load-pub nrg-key param-key] ; the on-change-val gets conj'd onto this
      :publications       (pubs/pubs-for-param nrg-key param-key)}]))

(defn param-publication-link
  ""
  [nrg-key param-key]
  (if-let [loaded-pub-link
           (:link @(rf/subscribe
                    [:nrg/loaded-pub nrg-key param-key]))]
    [:a {:href   loaded-pub-link
         :target "_blank"
         :rel    "noopener noreferrer"} "→ Quelle"]))

;; ################################
;; ####### Tabular Settings #######
;; ################################

(defn param-settings-tabular
  "Publication-Dropdown and Inputs
  for one combination
  of energy-source and parameter"
  [nrg-key parameter-dfn]
  [:div
   {:key (str nrg-key (first parameter-dfn))}
   [:div
    [:div.columns.is-vcentered.is-mobile
     [:div.column
      [param-input [:energy-sources nrg-key] parameter-dfn]]
     [:div.column [param-publication-link nrg-key (first parameter-dfn)]]]]
   [:div.mt-1 [param-dropdown nrg-key parameter-dfn]]])

(defn settings-table-row
  "Standard parameters for one Energy source"
  [[nrg-key nrg]]
  [:tr
   [:th.is-vcentered
    {:style    {:cursor "help"}
     :on-click (h/dispatch-on-x [:ui/scroll-to-explanation nrg-key])}
    (with-tooltip (:name nrg))]
   (map-indexed
    (fn [i parameter-dfn]
      [:td {:key i} [param-settings-tabular
                     nrg-key parameter-dfn]])
    params/common-nrg-parameters)])

(defn settings-table-top-row
  ""
  []
  [:tr  [:th]
   (map-indexed
    (fn [i [param-key parameter-dfn]]
      [:th.has-text-centered
       {:key      i
        :style    {:cursor "help"}
        :on-click (h/dispatch-on-x
                   [:ui/scroll-to-explanation param-key])}
       (with-tooltip (:name parameter-dfn))])
    params/common-nrg-parameters)])

(defn arealess-settings
  "Dropdown and Input for solar rooftop
  or onshore wind"
  [nrg-key]
  [:div.has-text-centered.mt-3
   {:style {:margin-left  "auto"
            :margin-right "auto"}}
   [:span.has-text-weight-bold
    {:on-click (h/dispatch-on-x [:ui/scroll-to-explanation nrg-key])}
    (with-tooltip (cfg/snippet :common-parameter-inputs
                               :arealess-capacity :name nrg-key))]

   [:div.columns.is-mobile.is-vcentered.mt-1
    [:div.column]
    [:div.column.is-narrow [arealess-input nrg-key]]
    [:div.column.is-narrow [arealess-dropdown nrg-key]]
    [:div.column.is-narrow [param-publication-link nrg-key :arealess-capacity]]
    [:div.column]]])

(defn detailed-settings-tabular
  "The table of Parameters settings.
  Shown only on larger Screens."
  []
  [:div#detailed-settings.is-hidden-touch
   (controlled-panel :details
                     "Parameter"
                     [:table.table
                      {:style {:margin-left  "auto"
                               :margin-right "auto"}}
                      [:thead
                       [settings-table-top-row]]
                      [:tbody
                       (for [nrg-source @(rf/subscribe [:nrg/get-all])]
                         ^{:key (first nrg-source)}
                         [settings-table-row nrg-source])]]

                     [arealess-settings :solar]
                     [arealess-settings :wind])])

;; ########################
;; ##### Explanations #####
;; ########################

(defn param-settings-pair-explanations
  "Publication Dropdown and Input for nrg-parameter"
  [nrg-key parameter-dfn]
  [:div.block
   {:key (str nrg-key (first parameter-dfn))}
   [:div.has-text-weight-bold.mb-1
    (:name (second parameter-dfn)) " "
    [param-publication-link nrg-key (first parameter-dfn)]]
   [:div.columns.is-mobile
    [:div.column
     [param-dropdown nrg-key parameter-dfn]]
    [:div.column.is-narrow
     [param-input [:energy-sources nrg-key] parameter-dfn]]]])

(defn params-for-nrg-explanations
  "On mobile " ;TODO:
  [nrg-key nrg]
  [:div.is-hidden-desktop
   [:h5.title.is-5 "Parameter für " (:name nrg) ":"]
   (into [:div ;TODO: make specials  generic

          (when (= nrg-key :solar) ; special case Solar: Inputs for Rooftop capacity
            [param-settings-pair-explanations
             nrg-key params/arealess-capacity-solar])

          (when (= nrg-key :wind) ; special case Wind: Inputs for capacity
            [param-settings-pair-explanations
             nrg-key params/arealess-capacity-wind])]
         (map
          (fn [parameter-dfn]
            [param-settings-pair-explanations nrg-key parameter-dfn])
          params/common-nrg-parameters))])

(defn format-explanation
  ([i exp-key]
   (format-explanation i exp-key nil))
  ([i exp-key supplement]
   (let [heading (get cfg/explanation-headings exp-key)
         text    (get cfg/texts exp-key)]
     [:div.block
      {:key i
       :id  (str "explanation-" (name exp-key))}
      [:h4.title.is-4 heading]
      [:div.content
       (h/dangerous-html text)]
      supplement])))

(defn explanations
  []
  [:div#detailed-settings.mt-4
   [controlled-panel :explanations
    [:<> "Erläuterungen" [:span.is-hidden-desktop " und Parameter"]]
    [:div.block
     [:h3.title.is-3 {:id "explanation-general"} (get cfg/explanation-headings :general)]
     [:div.content (h/dangerous-html (get cfg/texts :general))]]
    [:div.block
     [:h3.title.is-3 "Energiequellen"]
     (map-indexed (fn [i [nrg-key nrg]]
                    (format-explanation
                     i nrg-key (params-for-nrg-explanations nrg-key nrg))) cfg/nrgs)]
    [:h3.title.is-3 "Parameter"]
    (map-indexed
     format-explanation params/common-param-keys)]])

;; ######################
;; ##### Energy-Mix #####
;; ######################

(defn lock-button
  ""
  [nrg-key]
  [:button {:style    {:transform    "scale(0.75)"
                       :padding-top  "4px"
                       :padding-left "4px"}
            :on-click (h/dispatch-on-x [:nrg-share/toggle-lock nrg-key])}
   [:img {:width  "25px"
          :height "25px"
          :src    (if @(rf/subscribe [:nrg-share/locked? nrg-key])
                    "symbols/lock_closed.svg"
                    "symbols/lock_open.svg")}]])

(defn energy-slider
  "Single Slider to adjust the share of an Energy.
  Also renders: Lock Button, Icon and Text."
  [[nrg-key {:keys [name props share color]}]]
  [:div.eslider.pt-1 {:style {:background-color color
                              :width            "100%"}}

   ;; Above Slider
   [:div.columns.is-vcentered.is-gapless.mb-0.is-mobile
    ;; Lock-Button
    [:div.column.is-narrow
     [lock-button nrg-key]]

    ;; Icon
    [:div.column.is-narrow.mr-2.ml-1.mt-1
     [:img {:src   (cfg/icon-for-nrg nrg-key)
            :style {:height "1.5rem"}}]]
    ;; Text
    [:div.column.is-narrow
     [:label
      [:strong name " "
       (Math/round share) " % | "
       (Math/round
        @(rf/subscribe [:nrg-share/get-absolute-share nrg-key])) " TWh"]]]]

   ;; Actual Slider
   [:input {:type      "range" :min 0 :max 100
            :style     {:width "100%"}
            :value     (str share)
            :on-change (h/dispatch-on-x
                        [:nrg/remix-shares nrg-key])}]])

(defn energy-mix
  "Panel with Sliders to mix Energies"
  []
  (let [[bg-color font-color] @(rf/subscribe [:ui/decab-color])]
    [:div
     [:nav.panel
      ;; Headings with CO2-Indicator
      [:div.panel-heading {:style {:background-color bg-color
                                   :color            font-color}}
       [:div.columns.is-mobile [:div.column "Strommix"]
        [:div.column.has-text-right
         (if-let [co2-intensity @(rf/subscribe [:deriv/co2-per-kwh-mix])]
           (Math/round co2-intensity) "???")
         " g" co2 "/kWh"]]]

      ;; Sliders
      [:div.pt-3.pb-3.pr-3.pl-3
       [:div.mb-3
        [:div.columns.is-mobile.is-vcentered
         [:div.column.is-four-fifths
          "Stelle hier den Strommix der Zukunft zusammen…"]
         [:div.column.has-text-centered.is-clickable
          {:on-click (h/dispatch-on-x
                      [:global/initialize false])}
          [:img {:src   "symbols/reset.svg"
                 :width "40rem"}] [:br]
          "Reset"]]]

       (for [nrg-source @(rf/subscribe [:nrg/get-all])]
         ^{:key (first nrg-source)}
         [:div [energy-slider nrg-source]])]]]))

;; #########
;; ## Map ##
;; #########

(defn circle-by-radius
  ""
  [radius props]
  [:circle
   (merge
    {:r            (str radius) ; str avoids a NaN error
     :stroke       "black"
     :stroke-width "0"}
    props)])

(defn energy-label
  "Label and icon indicating the area occupied by the energy
  source associated with nrg-key"
  [opts nrg-key]
  (let [{:keys [props radius area relative-area color darker-color]}
        @(rf/subscribe [:deriv/data-for-map nrg-key])
        area-percent (-> relative-area
                         (* 1000)
                         Math/round
                         (/ 10))
        area         (Math/round area)]
    [:g
     [:image {:xlinkHref (cfg/icon-for-nrg nrg-key)
              :y         10
              :x         5
              :height    20}]
     [:text (merge {:zindex             1000
                    :alignment-baseline "central"
                    :font-weight        "bold"}
                   (if (:preview opts) {:font-size "1.5em"} {}))
      [:tspan {:x 33
               :y 15}
       (if (= 0 area-percent)
         "<0.1" area-percent) " %"]
      [:tspan {:x 33
               :y 35}
       (str
        (h/structure-int
         area) " km²")]]]))

(defn energy-on-map
  "Represents the Energy source associated with nrg-key
  by drawing a circle and label."
  [opts nrg-key]
  (let [{:keys [props radius area relative-area color darker-color]}
        @(rf/subscribe [:deriv/data-for-map nrg-key])
        circle-x (:cx props)
        circle-y (:cy props)]
    (when (> area 0)
      [:<>
       ;; Circle
       (circle-by-radius
        radius props)
       ;; Label
       (let [area           (Math/round area)
             area-percent   (-> relative-area
                                (* 1000)
                                Math/round
                                (/ 10))
             label-outside? (< radius 70)
             label-y        (if label-outside?
                              (- circle-y radius 40)
                              (- circle-y 15))
             label-x        (if label-outside?
                              (- circle-x 15)
                              (- circle-x 60))]
         [:svg
          {:x label-x :y label-y}
          (energy-label opts nrg-key)])])))

(defn arealess-indicator
  "Indicates produced energy without creating a
  corresponding circle. Used for offshore wind
  and rooftop solar."
  [nrg-key {:keys [color x y label]}]
  (let [hovering? (r/atom false)]
    (fn []
      (let [exhausted-arealess @(rf/subscribe [:nrg/exhausted-arealess nrg-key])
            main-text-props    {:filter      "url(#softGlow)"
                                :fill        color
                                :font-weight "bold"}
            common-props       {:text-anchor        "middle"
                                :zindex             1000
                                :alignment-baseline "central"
                                :cursor             "help"
                                :on-click           (h/dispatch-on-x [:ui/scroll-to-explanation nrg-key])
                                :on-mouse-over      #(reset! hovering? true)
                                :on-mouse-leave     #(reset! hovering? false)}]
        (when (> exhausted-arealess 0)
          [:g common-props
           (when @hovering?
             [:text {:x    x
                     :y    (- y 25)
                     :href "#"}
              "Wie wird das berechnet?"])
           [:text (merge main-text-props
                         {:x x
                          :y y})
            label]
           [:text (merge main-text-props
                         {:x x
                          :y (+ y 18)})
            (Math/round exhausted-arealess) " TWh"]])))))

(def svg-defs
  "Filters for use in the SVG MapView"
  [:defs
   [:filter#softGlow
    {:height "300%"
     :width  "300%"
     :x      "-75%"
     :y      "-75%"}
    [:feMorphology
     {:operator "dilate"
      :radius   "4"
      :in       "SourceAlpha"
      :result   "thicken"}]
    [:feGaussianBlur
     {:in           "thicken"
      :stdDeviation "3"
      :result       "blurred"}]
    [:feFlood
     {:flood-color "#555"
      :result      "glowColor"}]
    [:feComposite
     {:in       "glowColor"
      :in2      "blurred"
      :operator "in"
      :result   "softGlow_colored"}]
    [:feMerge
     [:feMergeNode
      {:in "softGlow_colored"}]
     [:feMergeNode
      {:in "SourceGraphic"}]]]])

(defn map-svg
  [{:keys [background-svg] :as opts}]
  (into [:svg.karte
         {:viewBox "0 0 640 876"
          :style (when-not background-svg
                   {:background-image "url('../imgs/deutschland2.svg')"})}

         (when background-svg
           [:svg {:viewBox "0 0 1000 1360" ;TODO: This hard codes Germany
                  :dangerouslySetInnerHTML {:__html background-svg}}])

         svg-defs
         (when-not (:preview opts)
           [:<>
            [arealess-indicator :wind {:label "Offshore-Wind"
                                       :x     430
                                       :y     40
                                       :color "rgba(135, 206, 250)"}]
            [arealess-indicator :solar {:label "Aufdach-PV"
                                        :x     540
                                        :y     600
                                        :color "yellow"}]])]
         ;; Circles and labels
        (doall (map (partial energy-on-map opts)
                    cfg/nrg-keys))))

(defn mapview
  "The actual SVG displaying the Content on the map.
  Background map come from CSS."
  []
  [:div.mapview
   [map-svg]])

;; ######################
;; ##### Indicators #####
;; ######################

(defn indicator
  "A graphical multi-colored indicator showing two things for a
  share-dependent parameter such as :deaths or :co2 :
  1. The absolute number of Deaths, Co2… for every energy source
  2. A graphical representation of the relative impact for each
  energy source."
  [heading param-key]
  (let [{:keys [param-total unit energy-sources formatter] :as nrg}
        @(rf/subscribe [:deriv/data-for-indicator param-key])
        unit (if unit (str " " unit))]
    [:div.mb-3
     ;; Text
     [:div
      ;; Heading + Absolute total
      [:strong
       {:on-click (h/dispatch-on-x [:ui/scroll-to-explanation param-key])}
       (with-tooltip heading) " " (formatter param-total) unit]
      ;; Absolute numbers per Energy-source
      (into [:div] (interpose " | "
                              (keep (fn [{:keys [name absolute]}]
                                      (when (> absolute 0)
                                        [:span
                                         name ": " (formatter absolute) [:nobr] unit]))
                                    (vals energy-sources))))]
     ;; Graphical Representation
     [:div
      (into [:svg
             {:width  "100%"
              :height "2em"}]
            (second
             (reduce
              (fn [[left-marg sofar] {:keys [name param-share color]}]
                [(+ left-marg
                    param-share) (conj sofar
                                       [:rect
                                        {:key    name
                                         :x      (str left-marg "%")
                                         :width  (str param-share "%")
                                         :height "2em"
                                         :style  {:fill         color
                                                  :stroke-width "0"
                                                  :stroke       "black"}}])])
              [0 []]
              (vals energy-sources))))]]))

(defn indicators
  []
  (controlled-panel
   :indicators "Weitere Ergebnisse"
   [indicator "Statistisch erwartbare Todesfälle pro Jahr:" :deaths]
   [indicator "Jährlicher Ressourcenverbrauch:" :resources]))

(defn savelinks
  ""
  []
  [:div
   [:div
    [:a {:href (js/encodeURI
                    @(rf/subscribe  [:save/csv-string]))}
     [:img {:src   "symbols/csv.svg"
            :style {:height "5rem"}}]
     ;; icons/csv
     ]]
   [:div
    [:a {:href (when (exists? js/window)
                 @(rf/subscribe  [:save/url-string]))}
     "→ Link, um diesen Strommix zu teilen"
     ]]
   [:div
    [:a {:href (str
                (get cfg/settings :preview-api)
                @(rf/subscribe [:save/preview-query-string]))}
     "→ Link to preview"]]
   [:div
    [:a {:href     (js/encodeURI
                    @(rf/subscribe  [:save/csv-string]))
         :download (str "strommix_"
                        (md5/string->md5-hex
                         (str @(rf/subscribe [:save/savestate])))
                        ".csv")} "→ Konfiguration als CSV herunterladen"]]])

;; ############################
;; ###### Main Component ######
;; ############################

(defn main-component []
  [:div
   [:p.is-size-5.has-text-centered
    (snippet :subtitle)]
   [:p.is-size-5.has-text-centered
    [:a {:on-click (h/dispatch-on-x [:ui/scroll-to-explanation :general])
         :style    {:cursor "pointer"}} (snippet :explanations)]]
   [:div.anwendung.pt-3.pb-3.pl-3.pr-3
    [:div.columns.is-centered ;.is-vcentered
     [:div.anzeige.column.is-two-thirds-desktop.has-text-centered
      [mapview]]
     [:div.column
      [energy-mix]
      [energy-needed]
      (when (cfg/feature-active? :bookmark-state)
        [savelinks])]]
    [indicator [:span "Jährlich anfallendes CO" [:sub "2"] ":"] :co2]
    [indicators]
    [detailed-settings-tabular]
    [explanations]]])
