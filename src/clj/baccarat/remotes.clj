(ns baccarat.remotes
  (:require [shoreleave.middleware.rpc :refer [defremote]]
            [baccarat.engine :as engine :refer [session]]))

(defremote handle-bet ;; WARNING: I'm lazy. No input validation being done here! Assuming no hackers/single user system.
  "Ajax endpoint. Send your bets here."
  [player-bet dealer-bet tie-bet panda-bet dragon-bet]
  (let [current-bet (engine/new-bet player-bet dealer-bet tie-bet panda-bet dragon-bet)
        new-state (engine/one-round @session current-bet)]
    (reset! session new-state)
    {:player-hand (:player-hand (last (:history @session)))
     :dealer-hand (:dealer-hand (last (:history @session)))
     :money (:money @session)}))
