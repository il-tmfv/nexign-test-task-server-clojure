(ns nexign-test-task-server-clojure.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :as ring-json]
            [ring.util.response :as rr]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def steam-api-key (System/getenv "STEAM_API_KEY"))

(println (str "Provided Steam API key: " steam-api-key))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (ring-json/wrap-json-response)
      (wrap-defaults site-defaults)))
