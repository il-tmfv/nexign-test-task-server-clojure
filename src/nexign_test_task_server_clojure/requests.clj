(ns nexign-test-task-server-clojure.requests
  (:require [clj-http.client :as client]
            [ring.util.response :as rr]))

(defn status-ok? [status]
  (< 199 status 400))

(defn get-steamid [steam-api-key username]
  (let [url (str
              "http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key="
              steam-api-key
              "&vanityurl="
              username)
        {:keys [body status]} (client/get url {:as :json :accept :json :throw-exceptions false})
        success (-> body :response :success)]
    (if (and (status-ok? status) (= success 1))
      (rr/response body)
      (-> (rr/response "Bad '/steamid' request")
          (rr/status 400)))))
