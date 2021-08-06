(ns ewr.color
  (:require
   [thi.ng.color.core :as col]
   [thi.ng.color.gradients :as grad]
   [thi.ng.math.core :as math]
   [clojure.string :as str]))

;; #################################
;; ####### Manipulate Colors #######
;; #################################

(defn make-transparent
  ""
  ([css-color]
   (make-transparent css-color 0.5))
  ([css-color transparency]
   (-> css-color
       (str/replace #" +" "")
       col/css
       col/as-rgba
       (assoc :a transparency)
       (col/as-css)
       deref)))

(defn set-brightness
  "Set the brightness of CSS color
  and returns a new CSS color string."
  [css-color target-brightness]
  (-> css-color
       (str/replace #" +" "")
       col/css
       col/as-hsla
       (assoc :l target-brightness)
       (col/as-css)
       deref))

;; ###############################################################
;; ############# Colors indicating the CO2-intensity #############
;; ###############################################################

(defn color-edges-to-gradients
  "Takes a sequence of CSS colors
  and reurns a sequence of gradients,
  i.e. binary sequences of th.ing-colors
  indicating the start and ends of gradients."
  [color-edges]
  (->> color-edges
       (#(conj % (last %)))
       (map col/css)
       (partition 2 1)))

(defn share-to-color
  "The maximum value will be mapped onto
  the end of the last gradient.
  0 will be mapped onto
  the beginnning of the first gradient.
  Share will be mapped to wherever it lands in between."
  [maximum share color-edges]
  (let [gradients (color-edges-to-gradients color-edges)
        normalized-share (-> share
                             (/ maximum)
                             (* (- (count gradients) 1)))
        gradient-n (Math/floor normalized-share) ; share falls in to gradient with this index
        ratio (- normalized-share gradient-n) ; it falls here within this gradient
        [color-a color-b] (nth gradients gradient-n)] ; pick the gradient
    (math/mix color-a color-b ratio))) ; and return the appropriate color

(defn contrasty-bw
  "Used for optimal text contrast."
  [incolor]
  (if (> (col/luminance incolor) 0.45)
    "#000000" "#ffffff"))

(def test-color-edges ["#2AA364", "#F5EB4D", "#9E4229", "#381D02"])


(comment
  (color-edges-to-gradients test-color-edges)
  (map
   #(share-to-color 100 % co2-gradients)
   (range 10 15)))

(comment (make-transparent "#dddddd" 0.5)
         (set-brightness "#dddddd" 0.5))
