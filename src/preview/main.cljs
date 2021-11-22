(ns preview.main
  (:require
   [ewr.serialization :as serialize]
   [reagent.dom.server :as rdom]
   [ewr.views :as views]   [ewr.reframing :as rfr]
   [re-frame.core :as rf]
   [preview.testdata :as testdata]
   ["sharp" :as sharp]
   ["fs" :as fs]
   ["nodejs-base64-converter" :as nodeBase64])
  (:require-macros [ewr.macros :as m]))

(defn svg->png
  [svg-string callback]
  (let [name     "testsvg"
        svg-name (str name ".svg")
        png-name (str name ".png")]
    (.writeFileSync fs svg-name svg-string)
    (-> svg-name
        sharp
        (.resize (clj->js {:width 1500}))
        .png
        (.toFile png-name callback))))

(defn svg-string->png-buf
  [svg-string callback]
  (let [svg-buf (.from js/Buffer svg-string)]
    (-> svg-buf
        sharp
        .png
        .toBuffer
        (.then callback))))

(rf/reg-event-db
 :save/savestate-into-db
 (fn [db [_ savestate]]
   (merge db savestate)))

(defn energy-text
  [offset i  [_key {:keys [name color share] :as nrg}]]
  (let [y-text  (+ 30 (* i offset))]
    [:<>
     [:text {:zindex    1000
             :font-size "3em"}
      [:tspan {:x 3 :y y-text} (str name ": " (Math/round share) "%")]]
     [:rect {:x 2 :y (+ 15 y-text)
             :stroke "black"
             :stroke-width 2
             :fill color
             :height 30 :width (* share 5)}]]))


(defn energy-needed
  []
  (let [energy-needed @(rf/subscribe [:energy-needed/get])]
    [:text {:zindex    1000
            :font-size "3em"}
     [:tspan {:x 3 :y 540} energy-needed " TWh"]]))

(defn co2
  []
  (let [co2-intensity @(rf/subscribe [:deriv/co2-per-kwh-mix])
        [bg-color font-color] @(rf/subscribe [:ui/decab-color])]

    [:g
     [:rect {:x 200 :y 505
             :stroke "black"
             :stroke-width 2
             :fill bg-color
             :height 50 :width 250}]
     [:text {:zindex    1000
             :font-size "3em"
             :fill font-color}
      [:tspan {:x 220 :y 540}
       (Math/round co2-intensity) " g" views/co2 "/kWh"]]]))

(defn energy-list
  []
  (let [offset  (/ 630 (+ 2 (count @(rf/subscribe [:nrg/get-all]))))]
    (into [:svg {:viewBox "0 0 1200 630"
                 :x 680 :y (/ offset 2)}
           [energy-needed]
           [co2]]
          (map-indexed (partial energy-text offset)
                       (into
                        (filter
                         (fn [[_ {:keys [share]}]]
                           (> (Math/round share) 0))
                         @(rf/subscribe [:nrg/get-all])))))))

(defn savestate-string->svg
  [savestate-string]
  (rf/dispatch-sync [:global/load-default-pubs])
  (when savestate-string
   (let [savestate
         (serialize/deserialize-savestate-string
          savestate-string)]
     (rf/dispatch-sync [:save/savestate-into-db savestate])))

  (let [svg (rdom/render-to-string
             [:svg {:xmlns      "http://www.w3.org/2000/svg"
                    :viewBox    "0 0 1200 630"
                    :xmlnsXlink "http://www.w3.org/1999/xlink"}
              [:rect {:width 1200 :height 630 :fill "white"}]
              [:svg {:x         "-29%" :y "-10%"
                     :transform "scale(1.25)"}
               [views/map-svg {:preview        true
                               :background-svg (m/slurp-file "resources/public/imgs/deutschland2.svg")}]]
              [energy-list]
              ])]
    svg))

(defn handler
  [event _context callback]
  (let [request          (js->clj event :keywordize-keys true)
        savestate-string (get-in request [:queryStringParameters :savestate])
        svg-string (savestate-string->svg savestate-string)]

    (when js/goog.DEBUG
      (svg-string->png-buf svg-string
                           #(.writeFileSync fs (str savestate-string ".png") %)))

    (svg-string->png-buf svg-string
                         #(callback
                           nil
                           (clj->js {:statusCode 200
                                     :body       (.encode nodeBase64 %)
                                     :headers    {"Content-Type" "image/png"}
                                     :isBase64Encoded true})))))

(defn ^:dev/after-load test-img-output
  []
  (handler testdata/test-request nil testdata/dummy-callback))