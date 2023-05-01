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

(defn cap-exceeded? [newval {:keys [cap]}]
  (when cap
    (> newval cap)))

(defn- delta-to-cap [nrg]
  (- (:share (second nrg)) (:cap (second nrg))))

(defn distribute-energy [amount nrgs]
  (loop [unprocessed-nrgs (sort-by delta-to-cap nrgs)
         remaining-amount amount
         processed-nrgs {}]
    (if-not (seq unprocessed-nrgs)
      processed-nrgs
      (let [[next-nrg-key {:keys [share cap] :as next-nrg}] (first unprocessed-nrgs)
            cumulated-shares (reduce #(+ %1 (:share (second %2))) 0 unprocessed-nrgs)
            relative-share (if (> cumulated-shares 0)
                             (/ share cumulated-shares)
                             (/ 1 (count unprocessed-nrgs)))
            share-delta (min (Math/round (* relative-share remaining-amount))
                             (- (or cap js/Infinity) share))
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

(defn attempt-remix
  "If remix is blocked, returns the energy-sources unchanged.
  Otherwise performs the remix"
  [changed-nrg-key newval energy-needed nrg-sources]
  (let [changed-nrg (get nrg-sources changed-nrg-key)]
    (cond
      (remix-blocked? changed-nrg-key newval nrg-sources) nrg-sources
      (cap-exceeded? newval changed-nrg)
      (remix-energy-shares-int changed-nrg-key (get changed-nrg :cap) nrg-sources)
      :else (remix-energy-shares-int changed-nrg-key newval nrg-sources))))


