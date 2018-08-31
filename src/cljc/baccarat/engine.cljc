(ns baccarat.engine)

(def decks 8)
(def shoe-size (* 52 decks))
(def point-values [1 2 3 4 5 6 7 8 9 0 0 0 0])
(def max-games 1500)
(def starting-money 10000) ;; TODO when you do the write-history-to-file route, make sure the history includes a starting struct
(def max-bet 100)
(def draw-table [[false false false false false false false false false false]
                [false false false false false false false false false false]
                [false false false false false false false false false false]
                [false false false false false false false false true false]
                [true true false false false false false false true true]
                [true true true true false false false false true true]
                [true true true true true true false false true true]
                [true true true true true true true true true true]])

(defn new-bet
  "A map representing a bid made by the gambler for this round."
  [player-bet dealer-bet tie-bet dragon-bet panda-bet]
  {:player-bet player-bet
   :dealer-bet dealer-bet
   :tie-bet tie-bet
   :dragon-bet dragon-bet
   :panda-bet panda-bet})

(defn new-record
  "Creates a recording of what happened in a particular round."
  [player-score dealer-score num-player-cards num-dealer-cards round-bet]
  {:player-score player-score
   :dealer-score dealer-score
   :num-player-cards num-player-cards
   :num-dealer-cards num-dealer-cards
   :round-bet round-bet})

(defn place-bet ;; TODO insurance for 7 and 8
  "Generates a bet for this round."
  [money]
  {:pre [number? money]}
  (let [choice (rand-int 3)
        units (rand-int (min max-bet money))]
    (cond (= choice 0) (new-bet units 0 0 0 0)
          (= choice 1) (new-bet 0 units 0 0 0)
          :else (new-bet 0 0 units 0 0))))

(defn num->value
  "Given a card number from the shoe, determine it's point value."
  [num]
  (nth point-values (mod num (count point-values))))

(defn dealer-draw?
  "Determine if the banker is supposed to draw a card or not."
  [banker-score, player-card]
  (nth (nth draw-table banker-score) player-card))

(defn new-game
  "Sets up the initial game-state."
  []
  {:shoe (shuffle (range shoe-size)) ;; perhaps apply num->value here, and for all future shoes?
   :history []
   :current-bet (new-bet 0 0 0 0 0)
   :money starting-money
   :games-played 0})

(defn shoe-check
  "Make sure the shoe has at least 6 cards loaded in prior to play."
  [shoe]
  {:pre [some? shoe]
   :post [some? %]}
  (if (< (count shoe) 7)
    (shuffle (range shoe-size)) ;; num->value here?
    shoe))

(defn update-money
  "Settle outstanding bets."
  [money record]
  (let [player-score (:player-score record)
        dealer-score (:dealer-score record)
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

(defn recalculate-money
  "Same as the other recalculate-money, just with a loop keyword."
  [history]
  (loop [history history
         money starting-money]
    (if (empty? history)
      money
      (recur (drop 1 history) (update-money money (first history))))))

(defn update-gs
  "Updates the game-state."
  [game-state new-shoe new-record]
  (-> game-state
      (assoc ,,, :shoe new-shoe)
      (update ,,, :games-played inc)
      (assoc ,,, :money (update-money (:money game-state) new-record))
      (assoc ,,, :history (conj (:history game-state) new-record))))

(defn one-round
  "Simulates a single round of play."
  [game-state]
  (let [current-bet (place-bet (:money game-state))
        shoe (shoe-check (:shoe game-state))
        first (num->value (nth shoe 1))
        second (num->value (nth shoe 2))
        third (num->value (nth shoe 3))
        fourth (num->value (nth shoe 4))
        fifth (num->value (nth shoe 5))
        sixth (num->value (nth shoe 6))
        player-score (mod (+ first second) 10)
        dealer-score (mod (+ third fourth) 10)]
    (if (or (>= player-score 8)
            (>= dealer-score 8))
      (update-gs game-state (drop 4 shoe) (new-record player-score dealer-score 2 2 current-bet)) ;; Update based on one hand having a natural win.
      (if (<= player-score 5)
        (let [new-player-score (mod (+ fifth player-score) 10)] ;; Player draws a card
          (if (dealer-draw? dealer-score fifth)
            (let [new-dealer-score (mod (+ sixth dealer-score) 10)] ;; Dealer draws a card
              (update-gs game-state (drop 6 shoe) (new-record new-player-score new-dealer-score 3 3 current-bet))) ;; Banker draws a third card based on player's third card
            (update-gs game-state (drop 5 shoe) (new-record new-player-score dealer-score 3 2 current-bet)))) ;; Banker does not draw a third card based on player's third card
        (if (<= dealer-score 5)
          (let [new-dealer-score (mod (+ fifth dealer-score) 10)]
            (update-gs game-state (drop 5 shoe) (new-record player-score new-dealer-score 2 3 current-bet))) ;; Dealer makes a vanilla draw
          (update-gs game-state (drop 4 shoe) (new-record player-score dealer-score 2 2 current-bet))))))) ;; Both hands don't take a card

(def session (atom (new-game)))

(defn simulate
  "Main simulation loop. Returns session history."
  [new-game]
  (loop [game-state new-game]
    (if (and (< (:games-played game-state) max-games)
             (pos? (:money game-state)))
      (recur (one-round game-state))
      (:history game-state))))

(defn calc-predicate
  [history predicate?]
  (count (filter predicate? history)))

(defn calc-stats
  "Calculates final statistics map for this session."
  [history]
  (let [calc (partial calc-predicate history)]
    {:player-wins (calc #(> (:player-score %) (:dealer-score %)))
     :dealer-wins (calc #(> (:dealer-score %) (:player-score %)))
     :ties (calc #(= (:player-score %) (:dealer-score %)))
     :games-played (count history)
     :pandas (calc #(= (:player-score %) 8))
     :dragons (calc #(= (:dealer-score %) 7))
     :successful-player-bets (calc #(and (pos? (get-in % [:round-bet :player-bet]))
                                         (> (:player-score %) (:dealer-score %))))
     :successful-dealer-bets (calc #(and (pos? (get-in % [:round-bet :dealer-bet]))
                                         (> (:dealer-score %) (:player-score %))))
     :successful-tie-bets (calc #(and (pos? (get-in % [:round-bet :tie-bet]))
                                      (= (:player-score %) (:dealer-score %))))
     :successful-panda-bets (calc #(and (pos? (get-in % [:round-bet :panda-bet]))
                                        (= (:player-score 8))))
     :successful-dragon-bets (calc #(and (pos? (get-in % [:round-bet :dragon-bet]))
                                         (= (:dealer-score %) 7)))
     :money (recalculate-money history)}))

;;  (-> (new-game)
;;      (simulate)
;;      (calc-stats)
;;      (print-stats))
