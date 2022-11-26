(ns ewr.remix
  (:require [ewr.helpers :as h]))

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

(defn relative-share-to-twh [energy-needed share]
  (* energy-needed share 0.01))

(defn twh-to-relative-share [energy-needed share-in-twh]
  (* 100 (/ share-in-twh energy-needed)))

(defn cap-exceeded? [energy-needed [_ {:keys [cap share]} :as nrg]]
  (when
   (and cap
        (< cap (relative-share-to-twh energy-needed share)))
    nrg))

(defn remix-energy-shares-float
  "Takes a set of energy shares (NRGS) and returns
  a new one with the share of CHANGED-NRG-KEY
  set to NEWVAL. Unblocked energie shares are rebalanced
  accordingly."
  [changed-nrg-key newval energy-needed nrgs]
  (let [;; nrgs that need to change to compensate the change made by the user
        sum-shares     (partial
                        transduce (map (comp :share second)) +)
        unlocked-nrgs  (into {}
                             (filter
                              #(not (:locked? (second %)))
                              nrgs))
        unlocked-share (sum-shares unlocked-nrgs)
        newval         (if (> newval (- unlocked-share 0.01))
                         unlocked-share newval)
        reacting-nrgs  (dissoc unlocked-nrgs changed-nrg-key)
        ;; The current total share of these reacting energies
        reacting-share (sum-shares reacting-nrgs)
        ;; The later total share of these reacting energies (after compensation
        reacted-share  (-> nrgs
                           (get-in [changed-nrg-key :share])
                           (- newval)
                           (+ reacting-share))
        ;; avoid extremely small values for reactions
        reacted-share  (if (< reacted-share 0.1) 0 reacted-share)
        reacted-energies (reduce
                          (fn [nrgs [reacting-nrg-key reacting-nrg]]
                            (let [scalefactor
                                  (cond
                                    (= reacted-share 0)  ; account for test case #3
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
                          reacting-nrgs)]
    (let [exceeding-nrg (some (partial cap-exceeded? energy-needed)
                              reacted-energies)]
      (if exceeding-nrg
        (remix-energy-shares-float
         (first exceeding-nrg)
         (twh-to-relative-share energy-needed (:cap (second exceeding-nrg)))
         energy-needed reacted-energies)
        reacted-energies))))

(defn attempt-remix
  "If remix is blocked, reurns thes energy-sources unchanged.
  Otherwise performs the remix"
  [changed-nrg-key newval energy-needed nrg-sources]
  (if (remix-blocked? changed-nrg-key newval nrg-sources)
    nrg-sources
    (remix-energy-shares-float changed-nrg-key newval energy-needed  nrg-sources)))

;; ##############
;; ### Legacy ###
;; ##############

;; These functions were used in previous versions of the program
;; and are left here for future reference.

(defn remix-energy-shares-int
  "Remix-function based on a representation of shares
  as integers."
  [changed-nrg-key newval nrgs]
  (let [unlocked-nrgs                     (into {}
                                                (filter
                                                 #(not (:locked? (second %)))
                                                 nrgs))
        unlocked-share                    (transduce (map (comp :share second))
                                                     + unlocked-nrgs)
        reacting-nrgs                     (dissoc unlocked-nrgs changed-nrg-key)
        freed-share                       (- (get-in nrgs [changed-nrg-key :share]) ; if negative
                                             newval) ; would more adequately be called "grabbed share"
        reacting-share                    (transduce (map (comp :share second))
                                                     + reacting-nrgs)]
    (if (> newval unlocked-share)
      nrgs ; return unchanged
      (first ; extract only the remixed energies
       (reduce
        (fn [[nrgs
              rem-free-share
              rem-reacting-nrgs] next-nrg-key]
          (let [rem-reacting-share (transduce (map (comp :share second))
                                              + rem-reacting-nrgs)
                old-share          (get-in nrgs [next-nrg-key :share])
                difference         (cond
                                     (= 0 rem-reacting-share)
                                     (Math/round (/ rem-free-share
                                                    (count rem-reacting-nrgs)))
                                     :else
                                     (Math/round
                                      (* rem-free-share
                                         (/ old-share
                                            rem-reacting-share))))
                new-share          (+ old-share difference)]
            [(assoc-in nrgs [next-nrg-key :share]
                       new-share)
             (- rem-free-share difference)
             (dissoc rem-reacting-nrgs next-nrg-key)]))
        [(assoc-in nrgs [changed-nrg-key :share]
                   newval)
         freed-share
         reacting-nrgs]
        (keys reacting-nrgs))))))

(defn- split-by-boolean
  ""
  [f coll]
  (let [grouped   (group-by f coll)
        emptycoll (empty coll)]
    [(into emptycoll (get grouped true))
     (into emptycoll (get grouped false))]))

(defn map-vals
  ""
  [f coll]
  (reduce (fn [sofar [key val]]
            (assoc sofar key (f val))) {} coll))
