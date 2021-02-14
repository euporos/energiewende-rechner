(ns stromrechner.logic
  (:require [stromrechner.helpers :as h]))


;; ######################
;; ##### Energy-mix #####
;; ######################

(defn remix-blocked?
  "Checks if the requested change of one energy's share
  can be made respecting the current blockings"
  [changed-nrg-key newval nrg-sources]
  (or (get-in nrg-sources [changed-nrg-key :locked?])
      (>= (count (filter #(get (second %) :locked?) nrg-sources))
       (dec (count nrg-sources)))))

(defn remix-energy-shares-float
  "Takes a set of energy shares (NRGS) and returns
  a new one with the share of CHANGED-NRG-KEY
  set to NEWVAL. Unblocked energie shares are rebalanced
  accordingly."
  [changed-nrg-key newval nrgs]
  (let [;; nrgs that need to change to compensate the change made by the user
        sum-shares (partial
                    transduce (map (comp :share second)) +)
        unlocked-nrgs (into {}
                            (filter
                             #(not (:locked? (second %)))
                             nrgs))
        unlocked-share (sum-shares unlocked-nrgs)
        newval (if (> newval (- unlocked-share 0.01))
                 unlocked-share newval)
        reacting-nrgs (dissoc unlocked-nrgs changed-nrg-key)
        ;; The current total share of these reacting energies
        reacting-share (sum-shares reacting-nrgs)
        ;; The later total share of these reacting energies (after compensation
        reacted-share (-> nrgs
                          (get-in [changed-nrg-key :share])
                          (- newval)
                          (+ reacting-share))
        ;; avoid extremely small values for reactions
        reacted-share (if (< reacted-share 0.1) 0 reacted-share)]

    ;; (js/console.log "unlocked share is " unlocked-share)    
    (reduce
     (fn [nrgs [reacting-nrg-key reacting-nrg]]
       (let [scalefactor
             (cond
               (= reacted-share 0) ; account for test case #3
               0 
               (= reacting-share 0) ; account for test case #4
               (/ 1 (count reacting-nrgs)) 
               :else
               (/ (:share reacting-nrg)
                reacting-share))]
         (assoc-in nrgs
                   [reacting-nrg-key :share]  
                   (* reacted-share
                      scalefactor))))
     (assoc-in nrgs [changed-nrg-key :share] newval) ; update the nrg changed by user
     reacting-nrgs)))
 
(defn attempt-remix
  ""
  [changed-nrg-key newval nrg-sources]
  (if (remix-blocked? changed-nrg-key newval nrg-sources)
    nrg-sources
    (remix-energy-shares-float changed-nrg-key newval nrg-sources)))


;; #######################
;; ##### Derivations #####
;; #######################


(defn- absolute-x
  "key should be :co2 or :deaths"
  [key energy-needed nrg]
  (-> (:share nrg)
      (/ 100)            ;TODO: from const
      (* energy-needed)  ; TWh of this nrg
      (* (key nrg))))

(defn- add-absolutes
  ""
  [key abs-key energy-needed nrgs]
  (h/map-vals
   (fn [nrg]
     (assoc nrg abs-key
            (absolute-x key energy-needed nrg)))
   nrgs))

(defn- calc-total
  ""
  [abs-key abs-added]
  (reduce #(+ %1 (abs-key (second %2)))
          0 abs-added))

(defn- add-share-of-x
  ""
  [abs-key share-key total abs-added]
  (h/map-vals
          #(assoc % share-key
                  (-> (abs-key %)
                      (/ total)
                      (* 100)
                      (h/nan->0))) ;TODO: from const
          abs-added))
  

;; ##############
;; ### Legacy ###
;; ##############

;; These functions were used in previous versions of the program
;; and are left here for future reference.

(defn remix-energy-shares-int
  "Remix-function based on a representation of shares
  as integers."
  [changed-nrg-key newval nrgs]
  (let [unlocked-nrgs (into {}
                            (filter
                               #(not (:locked? (second %)))
                               nrgs))
        unlocked-share (transduce (map (comp :share second))
                                  + unlocked-nrgs)
        reacting-nrgs (dissoc unlocked-nrgs changed-nrg-key)
        freed-share (- (get-in nrgs [changed-nrg-key :share]) ; if negative 
                       newval) ; would more adequately be called "grabbed share"
        reacting-share (transduce (map (comp :share second))
                                  + reacting-nrgs)]    
    (if (> newval unlocked-share)
      nrgs ; return unchanged
     (first                          ; extract only the remixed energies
      (reduce
       (fn [[nrgs
             rem-free-share
             rem-reacting-nrgs] next-nrg-key]
         (let [rem-reacting-share (transduce (map (comp :share second))
                                             + rem-reacting-nrgs)
               old-share (get-in nrgs [next-nrg-key :share])
               difference (cond
                            (= 0 rem-reacting-share)
                            (Math/round (/ rem-free-share
                                           (count rem-reacting-nrgs)))
                            :else
                            (Math/round
                             (* rem-free-share
                                (/ old-share
                                   rem-reacting-share))))
               new-share (+ old-share difference)]
           [(assoc-in nrgs [next-nrg-key :share]
                      new-share)
            (- rem-free-share difference)
            (dissoc rem-reacting-nrgs next-nrg-key)]))
       [(assoc-in nrgs [changed-nrg-key :share]
                  newval)
        freed-share
        reacting-nrgs]
       (keys reacting-nrgs))))))


(defn- split-by-boolean ; ultimately unused
  ""
  [f coll]
  (let [grouped (group-by f coll)
        emptycoll (empty coll)]
    [(into emptycoll (get grouped true))
     (into emptycoll (get grouped false))]))

(defn map-vals
  ""
  [f coll]
  (reduce (fn [sofar [key val]]
            (assoc sofar key (f val))) {} coll))
 
