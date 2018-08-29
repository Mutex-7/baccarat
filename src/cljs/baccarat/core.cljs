(ns baccarat.core
  (:require [reagent.core :as reagent]
            [cljsjs.marked]
            [clojure.string]))

(defn default-box
  "Just a default component"
  []
  [:div "This is some text."])

(defn ^:export init []
  (reagent/render [default-box] (.getElementById js/document "content") ))
