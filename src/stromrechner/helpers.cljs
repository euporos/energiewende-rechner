(ns stromrechner.helpers
  (:require
   [re-frame.core :as rf :refer [reg-event-db reg-sub]]
   [clojure.string :as str]))

(defn map-vals
  ""
  [f coll]
  (reduce
   (fn [sofar [key val]]
     (assoc sofar key (f val)))
   {} coll))

(defn reverse-paths
  ""
  [indata]
  (let [first-level-keys (keys indata)
        second-level-keys (keys (reduce merge (map second indata)))
        paths (for [flk first-level-keys
                    slk second-level-keys]
                [flk slk])]
    
    (reduce
     (fn [sofar nextpath]
       (assoc-in sofar (vec (reverse nextpath))
                 (get-in indata nextpath)))
     {} paths)))

(defn nan->nil
  ""
  [val]
  (if (js/isNaN val) nil val))

(defn nan->0
  ""
  [val]
  (if (js/isNaN val) 0 val))




(defn structure-int
  "Structures large integers
  by interposing it with whitespace"
  [integer]
  (if (= 0 integer)
    "0"
   (str/replace 
    (->> integer
         str
         reverse
         (partition 3 3 (repeat "0"))
         (interpose " ")
         flatten
         reverse
         (apply str)) #"^0*" "")))

(defn dispatch-on-x
  ""
  ([event]
   (dispatch-on-x nil event))
  ([sync? event]
   (dispatch-on-x sync? event nil))
  ([sync? event after-fn]
   #(let [newval (-> % .-target .-value)]
      (.preventDefault %)
      (when after-fn (after-fn))
      ((if sync?
         rf/dispatch-sync
         rf/dispatch) (conj event newval)))))


(defn dangerous-html
  ""
  [htmlstring]
  [:div {:dangerouslySetInnerHTML
        {:__html
         htmlstring}}])
