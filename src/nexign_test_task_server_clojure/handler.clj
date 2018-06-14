(ns nexign-test-task-server-clojure.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :as ring-json]
            [ring.util.response :as rr]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [nexign-test-task-server-clojure.requests :as requests]))

(def steam-api-key (System/getenv "STEAM_API_KEY"))

(println (str "Provided Steam API key: " steam-api-key))

(defn content-type-and-cors-response [response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/json")
      (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
      (assoc-in [:headers "Access-Control-Allow-Headers"] "Origin, X-Requested-With, Content-Type, Accept")))

(defn wrap-content-type-and-cors [handler]
  (fn
    ([request]
     (-> (handler request) (content-type-and-cors-response)))
    ([request respond raise]
     (handler request #(respond (content-type-and-cors-response %)) raise))))

(defroutes app-routes
           (GET "/steamid" [username]
             (requests/get-steamid steam-api-key username))
           (route/not-found "Not Found"))

(def app
  (-> app-routes
      (ring-json/wrap-json-response)
      (wrap-content-type-and-cors)
      (wrap-defaults site-defaults)))
