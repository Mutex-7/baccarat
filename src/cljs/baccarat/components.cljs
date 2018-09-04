(ns baccarat.components
  (:require [reagent.core :as reagent]
            [cljsjs.marked]
            [clojure.string]
            [baccarat.engine :as engine]
            [cljs.reader :refer [read-string]]
            [shoreleave.remotes.http-rpc :refer [remote-callback]])
  (:require-macros [shoreleave.remotes.macros :as macros]))

(def stats (reagent/atom {:player-wins 15
                          :dealer-wins 16
                          :ties 2
                          :games-played 33
                          :pandas 2
                          :dragons 1}))

(def last-round (reagent/atom {:player-hand [1 2 3]
                               :dealer-hand [3 2 1]}))

(def current-bet (reagent/atom {:player-bet 0
                                :dealer-bet 0
                                :tie-bet 0
                                :panda-bet 0
                                :dragon-bet 0}))

(def money (reagent/atom engine/starting-money))

(defn cockroach-pig
  "Displays cockroach pig."
  []
  [:div "Cockroach pig goes here."])

(defn small-road
  "Displays small road."
  []
  [:div "Small road goes here."])

(defn big-eye-boy
  "Displays big eye boy."
  []
  [:div "Big eye boy goes here."])

(defn big-road
  "Displays big road."
  []
  [:div "Big road goes here."])

(defn bead-plate
  "Displays bead plate."
  []
  [:div "Bead plate goes here."])

;; TODO
;; convert shoe number to actual card
;; indicate result of hand (player, dealer, tie)
;; indicate diff in money
(defn round-results
  "Displays results of last hand."
  [last-round]
  [:div {:id round-results}
   [:div "Round results:"]
   [:div "Players cards were: " (for [card (:player-hand last-round)] card)]
   [:div "Dealers cards were: " (for [card (:dealer-hand last-round)] card)]])

(defn bet-update
  "Places results of bet into reagent atoms."
  [bet-results]
  (reset! money (:money bet-results))
  (swap! last-round assoc :player-hand (:player-hand bet-results))
  (swap! last-round assoc :dealer-hand (:dealer-hand bet-results)))

(defn send-bet
  "Sends bet to the server side via ajax."
  []
  (let [player-bet (js/parseInt (:player-bet @current-bet))
        dealer-bet (js/parseInt (:dealer-bet @current-bet))
        tie-bet (js/parseInt (:tie-bet @current-bet))
        panda-bet (js/parseInt (:panda-bet @current-bet))
        dragon-bet (js/parseInt (:dragon-bet @current-bet))]
    (cond (not (nat-int? player-bet)) (js/alert "Player bet must be a non-negative, whole number.")
          (not (nat-int? dealer-bet)) (js/alert "Dealer bet must be a non-negative, whole number.")
          (not (nat-int? tie-bet)) (js/alert "Tie bet must be a non-negative, whole number.")
          (not (nat-int? panda-bet)) (js/alert "Panda insurance must be a non-negative, whole number.")
          (not (nat-int? dragon-bet)) (js/alert "Dragon insurance must be a non-negative, whole number.")
          (< @money (+ player-bet dealer-bet tie-bet panda-bet dragon-bet)) (js/alert "Sum of total bets placed exceeds your current funds.")
          :else (remote-callback :handle-bet
                                 [player-bet dealer-bet tie-bet panda-bet dragon-bet]
                                 #(bet-update %)))))

(defn place-bets ;; Getting a bit long?
  "Use these textboxes to place your bets."
  []
  [:div {:id "place-bets"}
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
  [:div {:id stats-table}
   [:div "Statistics:"]
   [:div "Player wins: " (:player-wins stats)]
   [:div "Dealer wins: " (:dealer-wins stats)]
   [:div "Ties: " (:ties stats)]
   [:div "Games played: " (:games-played stats)]
   [:div "Pandas: " (:pandas stats)]
   [:div "Dragons: " (:dragons stats)]
   [:div "Money: " (:money stats)]])

(defn right-side
  "Displays right side of screen."
  []
  [:div {:id "right"}
   [bead-plate]
   [big-road]
   [big-eye-boy]
   [small-road]
   [cockroach-pig]])

(defn display-money
  [money]
  [:div "Your money is: " money])

(defn left-side
  "Displays left side of screen."
  []
  [:div {:id "left"}
   #_[stats-table stats]
   [place-bets]
   [display-money @money]
   [round-results @last-round]])

(defn full-screen
  "Just a default component"
  [stats]
  [:div {:id "fullscreen"}
   [left-side]
   #_[right-side]])

(defn ^:export init []
  (reagent/render [full-screen] (.getElementById js/document "content")))
