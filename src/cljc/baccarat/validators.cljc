(ns baccarat.validators
  (:require [baccarat.engine :as engine]
            [clojure.spec.alpha :as spec]))

(defn zero-total?
  [bet-map]
  (zero? (+ (:player-bet bet-map)
            (:dealer-bet bet-map)
            (:tie-bet bet-map)
            (:panda-bet bet-map)
            (:dragon-bet bet-map))))

(defn sufficient-funds?
  [bet-map money]
  (> money (+ (:player-bet bet-map)
              (:dealer-bet bet-map)
              (:tie-bet bet-map)
              (:panda-bet bet-map)
              (:dragon-bet bet-map))))

(defn total-bet
  [bet-map]
  (+ (:player-bet bet-map)
     (:dealer-bet bet-map)
     (:tie-bet bet-map)
     (:panda-bet bet-map)
     (:dragon-bet bet-map)))

(defn is-bet-map?
  [bet-map]
  (and (contains? bet-map :player-bet)
       (contains? bet-map :dealer-bet)
       (contains? bet-map :tie-bet)
       (contains? bet-map :panda-bet)
       (contains? bet-map :dragon-bet)))
