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

(defn cap-exceeded? [energy-needed cap share]
  (when cap
    (< cap
      ;; Subtracting 0.01 here avoids a bug, where ensure-caps
      ;; recurs indefinitely dure to float imprecision
       (- (h/relative-share-to-twh energy-needed share)
          0.01))))

(defn cap-of-nrg-exceeded? [energy-needed [_ {:keys [cap share]} :as nrg]]
  (cap-exceeded? energy-needed cap share))

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
    reacted-energies))

(defn ensure-caps [energy-needed nrgs]
  (let [exceeding-nrg (first (filter (partial cap-of-nrg-exceeded? energy-needed)
                                     nrgs))]
    (if exceeding-nrg
      (recur energy-needed
             (remix-energy-shares-float
              (first exceeding-nrg)
              (h/twh-to-relative-share energy-needed (:cap (second exceeding-nrg)))
              energy-needed nrgs))
      nrgs)))

(defn attempt-remix
  "If remix is blocked, reurns thes energy-sources unchanged.
  Otherwise performs the remix"
  [changed-nrg-key newval energy-needed nrg-sources]
  (cond
    (remix-blocked? changed-nrg-key newval nrg-sources) nrg-sources
    (cap-exceeded? energy-needed (get-in nrg-sources [changed-nrg-key :cap]) newval)
    (remix-energy-shares-float changed-nrg-key (h/twh-to-relative-share
                                                energy-needed (get-in nrg-sources [changed-nrg-key :cap]))
                               energy-needed  nrg-sources)
    :else (remix-energy-shares-float changed-nrg-key newval energy-needed  nrg-sources)))

;; ##############
;; ### Legacy ### 
;; ##############

;; These functions were used in previous versions of the program
;; and are left here for future reference.

(defn distribute-energy [amount nrgs]

  (loop [unprocessed-nrgs (seq nrgs) remaining-amount amount processed-nrgs {}]
    (if-not (seq unprocessed-nrgs)
      processed-nrgs
      (let [[next-nrg-key {:keys [share] :as next-nrg}] (first unprocessed-nrgs)
            cumulated-shares (reduce #(+ %1 (:share (second %2))) 0 unprocessed-nrgs)
            relative-share (/ share cumulated-shares)
            share-delta (Math/round (* relative-share remaining-amount))
            new-share (+ share share-delta)
            new-nrg (assoc next-nrg :share new-share)
            remaining-amount* (- remaining-amount share-delta)
            processed-nrgs* (assoc processed-nrgs next-nrg-key new-nrg)]
        (recur (rest unprocessed-nrgs)
               remaining-amount*
               processed-nrgs*)))))

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
        share-to-be-distributed                       (- (get-in nrgs [changed-nrg-key :share]) ; if negative
                                                         newval) ; would more adequately be called "grabbed share"
        ]
    (if (> newval unlocked-share)
      (remix-energy-shares-int changed-nrg-key unlocked-share nrgs)
      (merge
       (assoc-in nrgs [changed-nrg-key :share] newval)
       (distribute-energy share-to-be-distributed reacting-nrgs)))))

(defn map-vals
  ""
  [f coll]
  (reduce (fn [sofar [key val]]
            (assoc sofar key (f val))) {} coll))

