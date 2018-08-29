(ns baccarat.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found files resources]]
            [compojure.handler :refer [site]]
            [ring.util.response :as response]))

(defroutes handler
  (GET "/" [] (response/redirect "index.html")) ;; oh, cool. I can just do that.
  (files "/" {:root "target"}) ;; to serve static files/resources
  (resources "/" {:root "target"}) ;; to serve resources on the classpath
  (not-found "Page not found!")) ;; default 404 page

(def app
  (-> (var handler)
      (site)))
