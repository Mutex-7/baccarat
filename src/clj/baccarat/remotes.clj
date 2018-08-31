(ns baccarat.remotes
  (:require [shoreleave.middleware.rpc :refer [defremote]]
            [baccarat.engine :as engine]))

(defremote handle-bet
  "Ajax endpoint. Send your bets here."
  [player-bet dealer-bet tie-bet panda-bet dragon-bet]
  (println "Got a request."))
