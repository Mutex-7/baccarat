(ns baccarat.components
  (:require [reagent.core :as reagent]
            [cljsjs.marked]
            [clojure.string]
            [baccarat.engine :as engine]
            [cljs.reader :refer [read-string]]
            [baccarat.validators :as val]
            [shoreleave.remotes.http-rpc :refer [remote-callback]])
  (:require-macros [shoreleave.remotes.macros :as macros]))

(def stats (reagent/atom {:player-wins 0
                          :dealer-wins 0
                          :ties 0
                          :games-played 0
                          :pandas 0
                          :dragons 0}))

(def last-round (reagent/atom {:player-hand [0 1 2]
                               :dealer-hand [0 1 2]
                               :panda false
                               :dragon false
                               :money-diff 0}))

(def current-bet (reagent/atom {:player-bet 0
                                :dealer-bet 0
                                :tie-bet 0
                                :panda-bet 0
                                :dragon-bet 0}))

(def money (reagent/atom engine/starting-money)) ;; not always the case. make sure to try for @session's :money

(defn round-results
  "Displays results of last hand."
  [last-round]
  (let [player-score (engine/score (:player-hand @last-round))
        dealer-score (engine/score (:dealer-hand @last-round))
        winning-hand (cond (= player-score dealer-score) "Hand was a tie."
                           (> player-score dealer-score) "Player hand wins."
                           (< player-score dealer-score) "Dealer hand wins."
                           :else "No idea what just happened. Ask Mutex, give him your history file.")]
    [:div
     [:div "Round results:"]
     [:div "Player's cards were: " (for [card-num (:player-hand @last-round)]
                                     (str (engine/num->card card-num) " "))]
     [:div "Player's score was: " player-score]
     [:div "Dealer's cards were: " (for [card-num (:dealer-hand @last-round)]
                                     (str (engine/num->card card-num) " "))]
     [:div "Dealer's score was: " dealer-score]
     [:div "Money diff: " (:money-diff @last-round)]
     [:div winning-hand]
     (when (:panda @last-round)
       [:div "Panda insurance has paid off!"])
     (when (:dragon @last-round)
       [:div "Dragon insurance has paid off!"])]))

(defn bet-update
  "Places results of bet into reagent atoms."
  [bet-results]
  (if (string? bet-results) ;; If result was an error message
    (js/alert bet-results)
    (do
      (let [player-hand (:player-hand bet-results)
            dealer-hand (:dealer-hand bet-results)
            player-score (engine/score player-hand)
            dealer-score (engine/score dealer-hand)]
        (swap! last-round assoc :money-diff (- (:money bet-results) @money))
        (reset! money (:money bet-results))
        (swap! last-round assoc :player-hand player-hand)
        (swap! last-round assoc :dealer-hand dealer-hand)
        (if (and (= 8 player-score)
                 (pos? (js/Number (:panda-bet @current-bet))))
          (swap! last-round assoc :panda true)
          (swap! last-round assoc :panda false))
        (if (and (= 7 dealer-score)
                 (pos? (js/Number (:dragon-bet @current-bet))))
          (swap! last-round assoc :dragon true)
          (swap! last-round assoc :dragon false))
        (cond (> player-score dealer-score) (swap! stats update :player-wins inc)
              (< player-score dealer-score) (swap! stats update :dealer-wins inc)
              (= player-score dealer-score) (swap! stats update :ties inc))
        (swap! stats update :games-played inc)
        (when (= 8 player-score)
          (swap! stats update :pandas inc))
        (when (= 7 dealer-score)
          (swap! stats update :dragons inc))))))

(defn get-bets
  []
  (let [player-bet (js/Number (:player-bet @current-bet))
        dealer-bet (js/Number (:dealer-bet @current-bet))
        tie-bet (js/Number (:tie-bet @current-bet))
        panda-bet (js/Number (:panda-bet @current-bet))
        dragon-bet (js/Number (:dragon-bet @current-bet))
        bet-map (engine/new-bet player-bet dealer-bet tie-bet panda-bet dragon-bet)]
    (cond (not (nat-int? player-bet)) (js/alert "Player bet must be a non-negative, whole number.")
          (not (nat-int? dealer-bet)) (js/alert "Dealer bet must be a non-negative, whole number.")
          (not (nat-int? tie-bet)) (js/alert "Tie bet must be a non-negative, whole number.")
          (not (nat-int? panda-bet)) (js/alert "Panda insurance must be a non-negative, whole number.")
          (not (nat-int? dragon-bet)) (js/alert "Dragon insurance must be a non-negative, whole number.")
          (not (val/sufficient-funds? bet-map @money)) (js/alert "Sum of total bets placed exceeds your current funds.")
          (val/zero-total? bet-map) (js/alert "You cannot make a total bet of zero.")
          :else bet-map)))

(defn send-bet
  "Sends bet to the server side via ajax."
  []
  (if-let [bet-map (get-bets)]
    (remote-callback :handle-bet [bet-map] #(bet-update %))))

(defn place-bets ;; Getting a bit long/repetetive?
  "Use these textboxes to place your bets."
  []
  [:div
   [:div "Place your bets:"]
   [:form
    [:div "Player bet"]
    [:input {:id "player-bet"
             :type "text"
             :placeholder "0"
             :on-change #(swap! current-bet assoc :player-bet (-> %
                                                                  .-target
                                                                  .-value))}]
    [:div "Dealer bet"]
    [:input {:id "dealer-bet"
             :type "text"
             :placeholder "0"
             :on-change #(swap! current-bet assoc :dealer-bet (-> %
                                                                  .-target
                                                                  .-value))}]
    [:div "Tie bet"]
    [:input {:id "tie-bet"
             :type "text"
             :placeholder "0"
             :on-change #(swap! current-bet assoc :tie-bet (-> %
                                                               .-target
                                                               .-value))}]
    [:div "Panda eight"]
    [:input {:id "panda-bet"
             :type "text"
             :placeholder "0"
             :on-change #(swap! current-bet assoc :panda-bet (-> %
                                                                 .-target
                                                                 .-value))}]
    [:div "Dragon seven"]
    [:input {:id "dragon-bet"
             :type "text"
             :placeholder "0"
             :on-change #(swap! current-bet assoc :dragon-bet (-> %
                                                                  .-target
                                                                  .-value))}]
    [:input {:type "button"
             :value "Place bet"
             :on-click #(send-bet)}]]])

(defn stats-table
  "Displays running totals of various game stats."
  [stats]
  [:div
   [:div "Statistics:"]
   [:div "Player wins: " (:player-wins @stats)]
   [:div "Dealer wins: " (:dealer-wins @stats)]
   [:div "Ties: " (:ties @stats)]
   [:div "Games played: " (:games-played @stats)]
   [:div "Pandas: " (:pandas @stats)]
   [:div "Dragons: " (:dragons @stats)]])

(defn display-money
  [money]
  [:div "Your money is: " @money])

(defn left-side
  "Displays left side of screen."
  []
  [:div
   [stats-table stats]
   [place-bets]
   [display-money money]
   [round-results last-round]])

(defn full-screen
  "Just a default component"
  [stats]
  [:div
   [left-side]])

(defn ^:export init []
  (reagent/render [full-screen] (.getElementById js/document "content")))
