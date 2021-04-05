(ns stromrechner.color
  (:require
   [thi.ng.color.core :as col]
   [thi.ng.color.gradients :as grad]
   [thi.ng.math.core :as math]
   [clojure.string :as str]))

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
  ""
  [css-color target-brightness]
  (-> css-color
       (str/replace #" +" "")
       col/css
       col/as-hsla
       (assoc :l target-brightness)
       (col/as-css)
       deref))

(def co2-colors ["#2AA364", "#F5EB4D", "#9E4229", "#381D02"])

(def co2-gradients
  (->> co2-colors
       (#(conj % (last %)))
       (map col/css)
       (partition 2 1)
       ))

(defn share-to-color
  ""
  [maximum share gradients]
  (let [normalized-share (-> share (/ maximum) (* (- (count gradients) 1)))
        gradient-n (Math/floor normalized-share)
        ratio (- normalized-share gradient-n)
        [color-a color-b] (nth gradients gradient-n)]
    (js/console.log gradient-n)
    (math/mix color-a color-b ratio)))

(col/luminance
 (col/css "#F5EB4D"))


(defn contrasty-bw
  ""
  [incolor]
  (if (> (col/luminance incolor) 0.45)
    "#000000" "#ffffff"))

 

(comment

  (map
   #(share-to-color 100 % co2-gradients)
   (range 10 15))

  )





(comment (make-transparent "#dddddd" 0.5)
         (set-brightness "#dddddd" 0.5))



