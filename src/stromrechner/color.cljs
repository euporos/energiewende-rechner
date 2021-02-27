(ns stromrechner.color
  (:require
   [thi.ng.color.core :as col]
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


(comment (make-transparent "#dddddd" 0.5)
         (set-brightness "#dddddd" 0.5))



