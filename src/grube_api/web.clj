(ns grube-api.web
  (:require [compojure.core :refer [defroutes GET]]
            [ring.middleware.cors :as cors]
            [ring.middleware.reload :as reload]
            [org.httpkit.server :as kit]
            [grube-api.game :as game]))

(defn handle-new-client [client-id]
  (game/add-new-player client-id))

(defn on-receive [channel data]
  (prn @game/game)
  (kit/send! channel data))

(defn on-close [client-id _]
  (game/remove-player client-id))

(defn handler [request]
  (let [client-id (get-in request [:headers "sec-websocket-key"])]
    (kit/with-channel request channel
      (handle-new-client client-id)
      (kit/on-close channel #(on-close client-id %))
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
