(ns baccarat.stats
  (:require [baccarat.engine :as engine :refer [score]]))

(defn calc
  [history predicate?]
  (if (empty? history)
    0
    (count (filter predicate? history))))

(defn player-wins
  [history]
  (calc history #(> (score (:player-hand %))
                    (score (:dealer-hand %)))))

(defn dealer-wins
  [history]
  (calc history #(> (score (:dealer-hand %))
                    (score (:player-hand %)))))

(defn ties
  [history]
  (calc history #(= (score (:player-hand %))
                    (score (:dealer-hand %)))))

(defn all-pandas
  [history]
  (calc history #(= (score (:player-hand %)) 8)))

(defn all-dragons
  [history]
  (calc history #(= (score (:dealer-hand %)) 7)))

(defn player-bet-wins
  [history]
  (calc history #(and (pos? (get-in % [:round-bet :player-bet]))
                      (> (score (:player-hand %)) (score (:dealer-hand %))))))

(defn dealer-bet-wins
  [history]
  (calc history #(and (pos? (get-in % [:round-bet :dealer-bet]))
                      (> (score (:dealer-hand %)) (score (:player-score %))))))

(defn tie-bet-wins
  [history]
  (calc history #(and (pos? (get-in % [:round-bet :tie-bet]))
                      (= (score (:player-hand %)) (score (:dealer-hand %))))))

(defn panda-bet-wins
  [history]
  (calc history #(and (pos? (get-in % [:round-bet :panda-bet]))
                      (= (score (:player-hand %)) 8))))

(defn dragon-bet-wins
  [history]
  (calc history #(and (pos? (get-in % [:round-bet :dragon-bet]))
                      (= (score (:dealer-hand %)) 7))))

(defn update-money
  [money record]
  (let [player-score (score (:player-hand record))
        dealer-score (score (:dealer-hand record))
        round-bet (:round-bet record)
        player-bet (:player-bet round-bet)
        dealer-bet (:dealer-bet round-bet)
        panda-bet (:panda-bet round-bet)
        dragon-bet (:dragon-bet round-bet)
        tie-bet (:tie-bet round-bet)]
    (apply +
           (- money player-bet dealer-bet tie-bet panda-bet dragon-bet)
           (remove nil?
                   (list
                    (when (> player-score dealer-score)
                      (* 2 player-bet))
                    (when (< player-score dealer-score)
                      (* 2 dealer-bet))
                    (when (= player-score dealer-score)
                      (* 9 tie-bet))
                    (when (= player-score 8)
                      (* 26 panda-bet))
                    (when (= dealer-score 7)
                      (* 41 dragon-bet)))))))

(defn money
  [history]
  (loop [history history
         money engine/starting-money]
    (if (empty? history)
      money
      (recur (drop 1 history) (update-money money (first history))))))

(defn all-stats
  [history]
  {:player-wins (player-wins history)
   :dealer-wins (dealer-wins history)
   :ties (ties history)
   :games-played (count history)
   :all-pandas (all-pandas history)
   :all-dragons (all-dragons history)
   :player-bet-wins (player-bet-wins history)
   :dealer-bet-wins (dealer-bet-wins history)
   :tie-bet-wins (tie-bet-wins history)
   :panda-bet-wins (panda-bet-wins history)
   :dragon-bet-wins (dragon-bet-wins history)
   :money (money history)})
