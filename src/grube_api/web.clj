(ns grube-api.web
  (:require [compojure.core :refer [defroutes GET]]
            [ring.middleware.cors :as cors]
            [ring.middleware.reload :as reload]
            [org.httpkit.server :as kit]
            [grube-api.game :as game]
            [clojure.data.json :as json]))

(defonce clients (atom {}))

(add-watch
 clients :notify-other-clients
 (fn [_ _ _ _]
   (doseq [[client-id channel] @clients]
     (->> client-id
          game/game-for-player
          json/write-str
          (kit/send! channel)))))

(defn handle-new-client [channel client-id]
  (game/add-new-player client-id)
  (swap! clients assoc client-id channel))

(defn on-receive [channel data]
  (prn @clients)
  (kit/send! channel data))

(defn on-close [_ client-id _]
  (game/remove-player client-id)
  (swap! clients dissoc client-id))

(defn handler [request]
  (let [client-id (get-in request [:headers "sec-websocket-key"])]
    (kit/with-channel request channel
      (handle-new-client channel client-id)
      (kit/on-close channel #(on-close channel client-id %))
      (kit/on-receive channel #(on-receive channel %)))))

(defroutes app-routes
  (GET "/health" _ "Ok")
  (GET "/ws" [] handler))

(def app
  (-> #'app-routes
      (reload/wrap-reload) ;TODO dev only
      (cors/wrap-cors :access-control-allow-origin [#".*"]
                      :access-control-allow-methods [:get :put :post :delete]
                      :access-control-allow-credentials ["true"])))
