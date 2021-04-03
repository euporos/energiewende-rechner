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


(def nuclear
  [:svg
  {:x "0px" 
   :y "0px"
   :viewbox "0 0 508.021 508.021"
   ;; :style "enable-background:new 0 0 508.021 508.021;"
   }
   
  [:g 
   [:path 
    {:d "M507.711,254.033c0-35.3-39-66.1-100.3-85.3c23.4-75.9,25.5-142.2-10.2-162.8c-35-20.2-90,13.2-143.2,69.6
			c-53.2-56.4-108.2-89.8-143.2-69.6c-35.7,20.6-33.6,86.8-10.2,162.8c-61.3,19.3-100.3,50-100.3,85.3s39,66.1,100.3,85.3
			c-23.4,75.9-25.5,142.2,10.2,162.8c7,4.1,48.7,29.4,143.2-69.6c91.6,98.5,136.2,73.6,143.2,69.6c35.7-20.6,33.6-86.8,10.2-162.8
			C468.711,320.133,507.711,289.333,507.711,254.033z M389.01,20.133c25.6,14.8,24.4,72.7,2.5,144c-23.3-6.2-49.3-10.9-77.3-13.6
			c-15.5-22.4-32.1-43.7-49.1-62.8C350.911-2.667,384.611,17.633,389.01,20.133z M181.711,339.633c-21.5-2.7-41.7-6.6-60.2-11.4
			c6.1-17.9,13.4-36.4,21.8-55.2C149.51,285.833,173.01,328.033,181.711,339.633z M121.51,179.833c18.4-4.8,38.7-8.7,60.2-11.4
			c-7.6,10.4-33.1,54.8-38.4,66.6C134.911,216.233,127.611,197.733,121.51,179.833z M152.211,254.033c6.5-16.2,40.7-75.5,50.7-87.8
			c17.4-2.9,86.7-3,102.3,0c9.9,12.5,44.2,71.4,50.7,87.8c-6.4,16.1-40.7,75-50.7,87.8c-17.1,3-86.3,2.9-102.3,0
			C192.611,328.833,158.51,269.833,152.211,254.033z M364.711,273.033c8.4,18.8,15.7,37.3,21.8,55.2c-18.4,4.8-38.7,8.7-60.2,11.4
			C333.81,329.233,359.51,284.933,364.711,273.033z M326.31,168.533c21.5,2.7,41.7,6.6,60.2,11.4c-6.1,17.9-13.4,36.4-21.8,55.2
			C359.51,223.433,333.911,179.033,326.31,168.533z M254.01,99.933c13.1,14.8,26.3,31.3,39,48.9c-15.6-1.6-68-1.5-78,0
			C227.711,131.233,240.911,114.833,254.01,99.933z M119.01,20.133c27.8-18.8,82.5,23.7,123.9,67.6c-16.9,19.1-33.5,40.4-49.1,62.8
			c-28,2.8-54,7.4-77.3,13.6C94.51,92.833,94.411,36.733,119.01,20.133z M16.61,254.033c0-27.7,35.1-53,89.1-69.7
			c7.8,22.9,17.4,46.4,28.4,69.7c-11,23.2-20.6,46.8-28.4,69.7C51.81,307.033,16.61,281.833,16.61,254.033z M119.01,487.933
			c-25.6-14.8-24.4-72.7-2.5-144c23.3,6.2,49.3,10.8,77.3,13.6c15.5,22.4,32.1,43.7,49.1,62.8
			C192.81,473.633,144.211,502.533,119.01,487.933z M215.01,359.233c12.6,1.6,65.2,1.7,78,0c-12.7,17.6-25.8,34-39,48.8
			C240.911,393.233,227.81,376.833,215.01,359.233z M389.01,487.933c-25.3,14.6-73.8-14.4-123.9-67.6c17-19.1,33.6-40.4,49.1-62.8
			c28-2.8,54-7.4,77.3-13.6C413.51,415.233,414.711,473.133,389.01,487.933z M402.31,323.733c-7.8-22.9-17.4-46.4-28.4-69.7
			c11-23.2,20.6-46.8,28.4-69.7c53.9,16.7,89.1,41.9,89.1,69.7S456.211,307.033,402.31,323.733z"}]] 
  [:g 
   [:g 
    [:path 
     {:d "M254.01,204.933c-27.1,0-49.1,22-49.1,49.1c0,27.1,22,49.1,49.1,49.1c27.1,0,49.1-22,49.1-49.1
			C303.111,226.933,281.111,204.933,254.01,204.933z M254.01,286.833c-18.1,0-32.7-14.7-32.7-32.7c0-18.1,14.7-32.7,32.7-32.7
			s32.7,14.7,32.7,32.7S272.111,286.833,254.01,286.833z"}]]]])



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
 
