(ns nexign-test-task-server-clojure.requests
  (:require [clj-http.client :as client]
            [clojure.set :as set]
            [ring.util.response :as rr]))

(defn status-ok? [status]
  (< 199 status 400))

(def request-options {:as :json :accept :json :throw-exceptions false})

(defn get-steamid [steam-api-key username]
  (let [url (str
              "http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key="
              steam-api-key
              "&vanityurl="
              username)
        {:keys [body status]} (client/get url request-options)
        success (-> body :response :success)]
    (if (and (status-ok? status) (= success 1))
      (rr/response body)
      (-> (rr/response "Bad '/steamid' request")
          (rr/status 400)))))

(defn- get-owned-games [steam-api-key steamid]
  (let [url (str
              "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key="
              steam-api-key
              "&steamid="
              steamid
              "&format=json")
        {:keys [body status]} (client/get url request-options)]
    (when (status-ok? status)
      (set (map #(:appid %) (-> body :response :games))))))

(defn- get-all-owned-games [steam-api-key steamids]
  (apply set/intersection (pmap (partial get-owned-games steam-api-key) steamids)))

(defn- get-game-data [appid]
  (let [url (str "http://steamspy.com/api.php?request=appdetails&appid=" appid)
        {:keys [body status]} (client/get url request-options)]
    (when (and (status-ok? status) (contains? (:tags body) :Multiplayer))
      {:name (:name body) :appid (:appid body) :userscore (:userscore body) :genre (:genre body)})))

(defn- filter-only-multiplayer [appids]
  (->> appids
       (pmap get-game-data)
       (filter (complement nil?))))

(defn get-common-games [steam-api-key steamids]
  (->> steamids
       (get-all-owned-games steam-api-key)
       (filter-only-multiplayer)))
