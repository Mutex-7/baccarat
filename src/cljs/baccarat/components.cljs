(ns baccarat.components
  (:require [reagent.core :as reagent]
            [baccarat.engine :as engine]
            [baccarat.validators :as val]
            [baccarat.stats :as stats]
            [shoreleave.remotes.http-rpc :refer [remote-callback]])
  (:require-macros [shoreleave.remotes.macros :as macros]))

(def history (reagent/atom []))

(defn load-and-save
  "Component for load and save buttons."
  []
  [:div
   [:button {:id "load-button"
             :on-click #(remote-callback :load-game [] (fn [response]
                                                         (if (string? response)
                                                           (js/alert response)
                                                           (reset! history response))))}
    "Load game"]
   [:button {:id "save-button"
             :on-click #(remote-callback :save-game [] (fn [response]
                                                         (js/alert response)))}
    "Save game"]])

(defn round-results
  "Displays results of last hand."
  [history]
  (if-let [last-round (last history)]
    (let [player-score (engine/score (:player-hand last-round))
          dealer-score (engine/score (:dealer-hand last-round))
          winning-hand (cond (= player-score dealer-score) "Hand was a tie."
                             (> player-score dealer-score) "Player hand wins."
                             (< player-score dealer-score) "Dealer hand wins."
                             :else "No idea what just happened. Ask Mutex, give him your save file.")]
      [:div
       [:div "Round results:"]
       [:div "Player's cards were: " (for [card-num (:player-hand last-round)]
                                       (str (engine/num->card card-num) " "))]
       [:div "Player's score was: " player-score]
       [:div "Dealer's cards were: " (for [card-num (:dealer-hand last-round)]
                                       (str (engine/num->card card-num) " "))]
       [:div "Dealer's score was: " dealer-score]
       [:div "Money diff: " (if (= 1 (count history))
                              (- (stats/money history) engine/starting-money)
                              (- (stats/money history) (stats/money (butlast history))))]
       [:div winning-hand]
       (when (and (= 8 player-score)
                  (pos? (:panda-bet (:round-bet last-round))))
         [:div "Panda insurance has paid off!"])
       (when (and (= 7 dealer-score)
                  (pos? (:dragon-bet (:round-bet last-round))))
         [:div "Dragon insurance has paid off!"])])))

(defn validate-bet
  [bet money]
  (let [player-bet (js/Number (:player-bet bet))
        dealer-bet (js/Number (:dealer-bet bet))
        tie-bet (js/Number (:tie-bet bet))
        panda-bet (js/Number (:panda-bet bet))
        dragon-bet (js/Number (:dragon-bet bet))
        bet-map (engine/new-bet player-bet dealer-bet tie-bet panda-bet dragon-bet)]
    (cond (not (nat-int? player-bet)) (js/alert "Player bet must be a non-negative, whole number.")
          (not (nat-int? dealer-bet)) (js/alert "Dealer bet must be a non-negative, whole number.")
          (not (nat-int? tie-bet)) (js/alert "Tie bet must be a non-negative, whole number.")
          (not (nat-int? panda-bet)) (js/alert "Panda insurance must be a non-negative, whole number.")
          (not (nat-int? dragon-bet)) (js/alert "Dragon insurance must be a non-negative, whole number.")
          (not (val/sufficient-funds? bet-map money)) (js/alert "Sum of total bets placed exceeds your current funds.")
          (val/zero-total? bet-map) (js/alert "You cannot make a total bet of zero.")
          :else bet-map)))

(defn place-bets
  "Use these textboxes to place your bets."
  []
  (let [current-bet (reagent/atom {:player-bet 0
                                   :dealer-bet 0
                                   :tie-bet 0
                                   :panda-bet 0
                                   :dragon-bet 0})]
    (fn []
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
                 :on-click #(if-let [bet-map (validate-bet @current-bet (stats/money @history))]
                              (remote-callback :handle-bet [bet-map] (fn [response]
                                                                       (if (string? response)
                                                                         (js/alert response)
                                                                         (reset! history (conj @history response))))))}]]])))

(defn stats-table
  "Displays running totals of various game stats."
  [history]
  [:div
   [:div "Statistics:"]
   [:div "Player wins: " (stats/player-wins history)]
   [:div "Dealer wins: " (stats/dealer-wins history)]
   [:div "Ties: " (stats/ties history)]
   [:div "All pandas: " (stats/all-pandas history)]
   [:div "All dragons: " (stats/all-dragons history)]
   [:div "Games played: " (count history)]
   #_[:div "Player bet wins: " (stats/player-bet-wins history)]
   #_[:div "Dealer bet wins: " (stats/dealer-bet-wins history)]
   #_[:div "Tie bet wins: " (stats/tie-bet-wins history)]
   #_[:div "Panda bet wins: " (stats/panda-bet-wins history)]
   #_[:div "Dragon bet wins: " (stats/dragon-bet-wins history)]])

(defn record->bead
  [record]
  (let [player-score (engine/score (:player-hand record))
        dealer-score (engine/score (:dealer-hand record))]
    (cond (= player-score dealer-score) "T"
          (> player-score dealer-score) "P"
          (< player-score dealer-score) "D")))

(defn hist->beads
  [history]
  (map record->bead history))

(def table-size (* 6 2))
(defn bead-plate
  "Displays the bead plate."
  [history]
  (let [beads (take table-size (concat (take-last table-size (hist->beads history)) (take table-size (repeat "B"))))]
    [:div "Bead plate: " (for [bead beads]
                           ^{:key bead} (str bead " "))]))

(defn right-side
  "Displays various representations of historical data."
  []
  [:div
   [bead-plate @history]])

(defn left-side
  "Displays left side of screen."
  []
  [:div
   [stats-table @history]
   [place-bets]
   [:div "Your money is " (stats/money @history)]
   [round-results @history]
   #_[load-and-save]])

(defn full-screen
  "Just a default component"
  [stats]
  (remote-callback :history [] #(reset! history %))
  [:div
   [left-side]
   [right-side]])

(defn ^:export init []
  (reagent/render [full-screen] (.getElementById js/document "content")))
