(ns baccarat.remotes
  (:require [shoreleave.middleware.rpc :refer [defremote]]
            [baccarat.engine :as engine :refer [session]]
            [clojure.spec.alpha :as spec]
            [baccarat.validators :as val]
            [baccarat.stats :as stats]))

(spec/def ::player-bet int?)
(spec/def ::dealer-bet int?)
(spec/def ::tie-bet int?)
(spec/def ::panda-bet int?)
(spec/def ::dragon-bet int?)
(spec/def ::bet-map (spec/keys :req-un [::player-bet ::dealer-bet ::tie-bet ::panda-bet ::dragon-bet])) ;; use :req-un to ingore ::namespace issue
(spec/def ::pos-bet #(pos? (val/total-bet %)))
(spec/def ::current-bet (spec/and ::bet-map ::pos-bet))

(defremote handle-bet
  [current-bet]
  (if (and (spec/valid? ::bet-map current-bet)
           (val/sufficient-funds? current-bet (stats/money (:history @session))))
    (do
      (reset! session (engine/one-round @session current-bet))
      (last (:history @session)))
    "Invalid bet was placed."))

(defremote history
  []
  (:history @session))

(defremote load-game ;; TODO catch a failed FS read.
  []
  (reset! session (read-string (slurp "baccarat-save.edn")))
  (:history @session))

(defremote save-game
  []
  (spit "baccarat.edn" @session)
  "Game has been saved.")
