(ns baccarat.remotes
  (:require [shoreleave.middleware.rpc :refer [defremote]]
            [baccarat.engine :as engine :refer [session]]
            [clojure.spec.alpha :as spec]
            [baccarat.validators :as val]))

(spec/def ::player-bet int?)
(spec/def ::dealer-bet int?)
(spec/def ::tie-bet int?)
(spec/def ::panda-bet int?)
(spec/def ::dragon-bet int?)
(spec/def ::bet-map (spec/keys :req-un [::player-bet ::dealer-bet ::tie-bet ::panda-bet ::dragon-bet])) ;; use :req-un to ingore ::namespace issue
;;(spec/def ::bet-map #(val/is-bet-map? %))
(spec/def ::pos-bet #(pos? (val/total-bet %)))
(spec/def ::current-bet (spec/and ::bet-map ::pos-bet))

(defremote handle-bet
  [current-bet]
  (if (and (spec/valid? ::bet-map current-bet)
           ;;(not (val/zero-total? current-bet)) ;; shouldn't need this.
           (val/sufficient-funds? current-bet (:money @session)))
    (do
      (reset! session (engine/one-round @session current-bet))
      {:player-hand (:player-hand (last (:history @session)))
       :dealer-hand (:dealer-hand (last (:history @session)))
       :money (:money @session)})
    "Invalid bet was placed."))

(defremote ui-sync
  []
  (engine/calc-stats (:history @session)))

(defremote load-game
  []
  (reset! session (read-string (slurp "baccarat.edn")))
  (engine/calc-stats (:history @session)))

(defremote save-game
  []
  (spit "baccarat.edn" @session)
  "Game has been saved.")
