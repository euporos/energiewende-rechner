(ns ewr.helpers
  (:require
   [clojure.string :as str]
   [re-frame.core :as rf :refer [reg-event-db reg-sub]]))

(defn map-subset? [a-map b-map]
  (every? (fn [[k _ :as entry]] (= entry (find b-map k))) a-map))

(defn relative-share-to-twh [energy-needed share]
  (* energy-needed share 0.01))

(defn twh-to-relative-share [energy-needed share-in-twh]
  (* 100 (/ share-in-twh energy-needed)))

(defn classes
  ""
  [& classstrings]
  (str/join " " classstrings))

(defn map-vals
  ""
  [f coll]
  (reduce
   (fn [sofar [key val]]
     (assoc sofar key (f val)))
   {} coll))

(defn reverse-paths
  "Reverses paths within a nested data structure
  two levels deep. For example:
  {:a {:x 1
       :y 2}
   :b {:x 3
       :y 4}}
  →
  {:x {:a 1
      :b 3}
   :y {:a 2
      :b 4}}"
  [indata]
  (let [first-level-keys  (keys indata)
        second-level-keys (keys (reduce merge (map second indata)))
        paths             (for [flk first-level-keys
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
  by interposing it with whitespace.
  E.g. 1234567 → 1 234 567"
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
  "Returns a function that dispatches a Re-Frame event.
  The event is created by conjing the first argument of
  the returned function to the event vector.
  f can be a function the  will be applied to that argument
  before conjing.
  Suppresses the default effect."
  ([event]
   (dispatch-on-x false  event))
  ([sync? event]
   (dispatch-on-x sync? identity event))
  ([sync? f event]
   #(let [newval (-> % .-target .-value)]
      (.preventDefault %)
      ((if sync?
         rf/dispatch-sync
         rf/dispatch) (conj event (f newval))))))

(defn dangerous-html
  "Insert an HTML String directly into the DOM"
  [htmlstring]
  [:div {:dangerouslySetInnerHTML
         {:__html
          htmlstring}}])

(defn radius-from-area-circle
  "Given a circle's area
  returns it's radius."
  [area]
  (Math/sqrt (/ area Math/PI)))

(defn snippet-on-path
  "Extracts the snippet at PATH. If at any point
  of the path a string should be encountered it is returned."
  [structure & path]
  (reduce
   (fn [structure next-path-elem]
     (if (string? structure)
       (reduced structure)
       (get structure next-path-elem)))
   structure
   path))
