(ns baccarat.engine)

(def decks 8)
(def shoe-size (* 52 decks))
(def point-values [1 2 3 4 5 6 7 8 9 0 0 0 0])
(def starting-money 10000) ;; move these into a config.cljc?
(def draw-table [[true true true true true true true true true true] ;; TODO turn this into a function instead of a table?
                 [true true true true true true true true true true]
                 [true true true true true true true true true true]
                 [true true true true true true true true false true]
                 [false false true true true true true true false false]
                 [false false false false true true true true false false]
                 [false false false false false false true true false false]
                 [false false false false false false false false false false]])
(def faces ["A" "2" "3" "4" "5" "6" "7" "8" "9" "10" "J" "Q" "K"])
(def suits ["♠" "♣" "♥" "♦"])
(def face-values (into [] (mapcat identity
                                  (for [i (range (count suits))]
                                    (for [j (range (count faces))]
                                      {:suit (nth suits i)
                                       :number (nth faces j)})))))

(defn new-bet
  "A map representing a bid made by the gambler for this round."
  [player-bet dealer-bet tie-bet panda-bet dragon-bet]
  {:player-bet player-bet
   :dealer-bet dealer-bet
   :tie-bet tie-bet
   :panda-bet panda-bet
   :dragon-bet dragon-bet})

(defn new-record
  "Creates a recording of what happened in a particular round."
  [player-hand dealer-hand round-bet]
  {:player-hand player-hand
   :dealer-hand dealer-hand
   :round-bet round-bet})

(defn new-game
  "Sets up the initial game-state."
  []
  {:shoe (shuffle (range shoe-size))
   :history []})

(def session (atom (new-game)))

(defn num->value
  "Given a card number from the shoe, determine it's point value."
  [num]
  (nth point-values (mod num (count point-values))))

(defn num->card
  "Given a card number from the shoe, determine it's face value."
  [num]
  (let [card-map (nth face-values (mod num (count face-values)))]
    (str (:number card-map) (:suit card-map))))

(defn dealer-draw?
  "Determine if the banker is supposed to draw a card or not."
  [banker-score player-card]
  (nth (nth draw-table banker-score) player-card))

(defn shoe-check
  "Make sure the shoe has at least 6 cards loaded in prior to play."
  [shoe]
  {:pre [some? shoe] ;; TODO remove
   :post [some? %]}
  (if (< (count shoe) 7)
    (shuffle (range shoe-size))
    shoe))

(defn score
  "Determine the score of a hand."
  [hand]
  (loop [acc hand
         score 0]
    (if (seq acc)
      (recur (rest acc) (mod (+ score (num->value (first acc))) 10))
      score)))

(defn update-gs
  "Updates the game-state."
  [game-state new-shoe new-record]
  (-> game-state
      (assoc ,,, :shoe new-shoe)
      (assoc ,,, :history (conj (:history game-state) new-record))))

(defn one-round
  "Simulates a single round of play. Returns an updated game state."
  [game-state current-bet]
  (let [shoe (shoe-check (:shoe game-state))
        player-hand (vector (nth shoe 1) (nth shoe 2))
        dealer-hand (vector (nth shoe 3) (nth shoe 4))]
    (if (or (>= (score player-hand) 8)
            (>= (score dealer-hand) 8))
      (update-gs game-state (drop 4 shoe) (new-record player-hand dealer-hand current-bet)) ;; Update based on one hand having a natural win.
      (if (<= (score player-hand) 5)
        (let [new-player-hand (conj player-hand (nth shoe 5))] ;; Player draws a card
          (if (dealer-draw? (score dealer-hand) (num->value (nth shoe 5)))
            (let [new-dealer-hand (conj dealer-hand (nth shoe 6))] ;; Dealer draws a card
              (update-gs game-state (drop 6 shoe) (new-record new-player-hand new-dealer-hand current-bet))) ;; Banker draws a third card based on player's third card
            (update-gs game-state (drop 5 shoe) (new-record new-player-hand dealer-hand current-bet)))) ;; Banker does not draw a third card based on player's third card
        (if (<= (score dealer-hand) 5)
          (let [new-dealer-hand (conj dealer-hand (nth shoe 5))]
            (update-gs game-state (drop 5 shoe) (new-record player-hand new-dealer-hand current-bet))) ;; Dealer makes a vanilla draw
          (update-gs game-state (drop 4 shoe) (new-record player-hand dealer-hand current-bet))))))) ;; Both hands don't take a card
