(ns baccarat.validators
  (:require [baccarat.engine :as engine]
            [clojure.spec.alpha :as spec]))

(defn nil->zero
  [input]
  (if #?(:clj (nil? input)
         :cljs (js/isNaN input))
    0
    input))

(defn blank->zero
  [input]
  (if (zero? (count input))
    0
    input))

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

;; NOTE js version has js suckage.
(defn string->int
  [string]
  #?(:cljs (nil->zero (js/parseInt string))
     :clj (try
            (Integer/parseInt string)
            (catch Exception e
              0))))

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
