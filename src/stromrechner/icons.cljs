(ns stromrechner.icons)

(def basestyle
  {:display "inline-block"
   :width "2em"
   :height "2em"
   ;; :stroke-width "0"
   ;; :stroke "currentColor"
   ;; :fill "currentColor"
   ;; :line-height "1"
   :position "relative"
   :top ".05em"
   :margin-top ".3em"
   :margin-left ".2em"
   :vertical-align "middle"
   })


(def lock
  [:path 
   {:fill "currentColor" 
    :d "M12,17C10.89,17 10,16.1 10,15C10,13.89 10.89,13 12,13A2,2 0 0,1 14,15A2,2 0 0,1 12,17M18,20V10H6V20H18M18,8A2,2 0 0,1 20,10V20A2,2 0 0,1 18,22H6C4.89,22 4,21.1 4,20V10C4,8.89 4.89,8 6,8H7V6A5,5 0 0,1 12,1A5,5 0 0,1 17,6V8H18M12,3A3,3 0 0,0 9,6V8H15V6A3,3 0 0,0 12,3Z"}])

(def lock-filled
  [:path 
   {:fill "currentColor" 
    :d "M12,17A2,2 0 0,0 14,15C14,13.89 13.1,13 12,13A2,2 0 0,0 10,15A2,2 0 0,0 12,17M18,8A2,2 0 0,1 20,10V20A2,2 0 0,1 18,22H6A2,2 0 0,1 4,20V10C4,8.89 4.9,8 6,8H7V6A5,5 0 0,1 12,1A5,5 0 0,1 17,6V8H18M12,3A3,3 0 0,0 9,6V8H15V6A3,3 0 0,0 12,3Z"}])

(def lock-open
  [:path 
   {:fill "currentColor" 
    :d "M18,20V10H6V20H18M18,8A2,2 0 0,1 20,10V20A2,2 0 0,1 18,22H6C4.89,22 4,21.1 4,20V10A2,2 0 0,1 6,8H15V6A3,3 0 0,0 12,3A3,3 0 0,0 9,6H7A5,5 0 0,1 12,1A5,5 0 0,1 17,6V8H18M12,17A2,2 0 0,1 10,15A2,2 0 0,1 12,13A2,2 0 0,1 14,15A2,2 0 0,1 12,17Z"}])


(def sun
  [:g#Layer_1
   [:style 
    {:type "text/css"} ".st0 {stroke:#000000;stroke-width:3;stroke-linecap:round;stroke-miterlimit:10;}"]
   [:circle
    {:fill "#000000"
     :cx "28.35" 
     :cy "28.35" 
     :r "13.1"}] 
   [:line.st0 
    {:x1 "28.35" 
     :y1 "12.17" 
     :x2 "28.35" 
     :y2 "2.15"}] 
   [:line.st0 
    {:x1 "20.26" 
     :y1 "14.34" 
     :x2 "15.25" 
     :y2 "5.66"}] 
   [:line.st0 
    {:x1 "14.34" 
     :y1 "20.26" 
     :x2 "5.66" 
     :y2 "15.25"}] 
   [:line.st0 
    {:x1 "12.17" 
     :y1 "28.35" 
     :x2 "2.15" 
     :y2 "28.35"}] 
   [:line.st0 
    {:x1 "14.34" 
     :y1 "36.43" 
     :x2 "5.66" 
     :y2 "41.45"}] 
   [:line.st0 
    {:x1 "20.26" 
     :y1 "42.35" 
     :x2 "15.25" 
     :y2 "51.04"}] 
   [:line.st0 
    {:x1 "28.35" 
     :y1 "44.52" 
     :x2 "28.35" 
     :y2 "54.55"}] 
   [:line.st0 
    {:x1 "36.43" 
     :y1 "42.35" 
     :x2 "41.45" 
     :y2 "51.04"}] 
   [:line.st0 
    {:x1 "42.35" 
     :y1 "36.43" 
     :x2 "51.04" 
     :y2 "41.45"}] 
   [:line.st0 
    {:x1 "44.52" 
     :y1 "28.35" 
     :x2 "54.55" 
     :y2 "28.35"}] 
   [:line.st0 
    {:x1 "42.35" 
     :y1 "20.26" 
     :x2 "51.04" 
     :y2 "15.25"}] 
   [:line.st0 
    {:x1 "36.43" 
     :y1 "14.34" 
     :x2 "41.45" 
     :y2 "5.66"}]])




(defn icon
  ""
  [svgcontent]
  [:svg {:style basestyle
         ;; :viewBox "0 0 16px 16px"
         } svgcontent])



(defn icon2
  ""
  [color svgcontent]
  [:svg {:style {:display "inline-block"
                 :width "1em"
                 :height "1em"
                 ;; :stroke-width "0"
                 ;; :stroke "currentColor"
                 ;; :fill "currentColor"
                 ;; :line-height "1"
                 :position "relative"
                 :top ".05em"
                 :margin-top ".3em"
                 :margin-left ".2em"
                 :vertical-align "middle"
                 }
          :viewBox "0 0 56.69 56.69"
         } svgcontent])
 
