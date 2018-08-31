(ns baccarat.components
  (:require [reagent.core :as reagent]
            [cljsjs.marked]
            [clojure.string]
            [shoreleave.remotes.http-rpc :refer [remote-callback]])
  (:require-macros [shoreleave.remotes.macros :as macros]))

;; stats-table, place-bets and results go in the first column. The others go in the second column.
;; still need a save and load button, opens dialogue box, accepts .edn file.

(def stats (reagent/atom {:player-wins 15 :dealer-wins 16 :ties 2 :games-played 33
                          :pandas 2 :dragons 1 :money 502}))

(def last-round (reagent/atom {:player-cards [1 2 3]
                               :dealer-cards [3 2 1]}))

(def current-bet (reagent/atom {:player-bet 0
                                :dealer-bet 0
                                :tie-bet 0
                                :panda-bet 0
                                :dragon-bet 0}))

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

(defn round-results
  "Displays results of last hand."
  [last-round]
  [:div {:id round-results}
   [:p "Round results:"]
   [:p "Players cards were: " (for [card (:player-cards @last-round)] card)]
   [:p "Dealers cards were: " (for [card (:dealer-cards @last-round)] card)]])

(defn send-bet
  "Sends bet to the server side via ajax."
  []
  (let [player-bet (:player-bet @current-bet)
        dealer-bet (:dealer-bet @current-bet)
        tie-bet (:tie-bet @current-bet)
        panda-bet (:panda-bet @current-bet)
        dragon-bet (:dragon-bet @current-bet)]
    (remote-callback :handle-bet
                     [player-bet dealer-bet tie-bet panda-bet dragon-bet]
                     #(js/alert "Got answer back from server!"))))

(defn place-bets ;; break these up into their own components?
  "Use these textboxes to place your bets."
  []
  [:div {:id "place-bets"}
   [:p "Place your bets:"]
   [:form
    [:p "Player bet"]
    [:input {:id "player-bet"
             :type "text"
             :placeholder "0"
             :on-change #(swap! current-bet assoc :player-bet (-> %
                                                                  .-target
                                                                  .-value))}]
    [:p "Dealer bet"]
    [:input {:id "dealer-bet"
             :type "text"
             :placeholder "0"
             :on-change #(swap! current-bet assoc :dealer-bet (-> %
                                                                  .-target
                                                                  .-value))}]
    [:p "Tie bet"]
    [:input {:id "tie-bet"
             :type "text"
             :placeholder "0"
             :on-change #(swap! current-bet assoc :tie-bet (-> %
                                                                  .-target
                                                                  .-value))}]
    [:p "Panda eight"]
    [:input {:id "panda-bet"
             :type "text"
             :placeholder "0"
             :on-change #(swap! current-bet assoc :panda-bet (-> %
                                                                  .-target
                                                                  .-value))}]
    [:p "Dragon seven"]
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
   [:p "Statistics:"]
   [:p "Player wins: " (:player-wins @stats)]
   [:p "Dealer wins: " (:dealer-wins @stats)]
   [:p "Ties: " (:ties @stats)]
   [:p "Games played: " (:games-played @stats)]
   [:p "Pandas: " (:pandas @stats)]
   [:p "Dragons: " (:dragons @stats)]
   [:p "Money: " (:money @stats)]])

(defn right-side
  "Displays right side of screen."
  []
  [:div {:id "right"}
   [bead-plate]
   [big-road]
   [big-eye-boy]
   [small-road]
   [cockroach-pig]])

(defn left-side
  "Displays left side of screen."
  []
  [:div {:id "left"}
   #_[stats-table stats]
   [place-bets]
   [round-results last-round]])

(defn full-screen
  "Just a default component"
  [stats]
  [:div {:id "fullscreen"}
   [left-side]
   #_[right-side]])

(defn ^:export init []
  (reagent/render [full-screen] (.getElementById js/document "content")))
