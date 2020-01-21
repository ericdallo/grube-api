(ns grube-api.web
  (:require [clojure.core.async :refer [go thread]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [grube-api.game :as game]
            [grube-api.in-handler :as in-handler]
            [grube-api.out-handler :as out-handler]
            [grube-api.json :as json]
            [ring.middleware.cors :as r.cors]
            [ring.middleware.defaults :as r.defaults]
            [ring.middleware.reload :as r.reload]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :as http-kit])
  (:import java.util.UUID))

(declare channel-socket)
(declare out-fn)

(defn random-uuid [_]
  (str (UUID/randomUUID)))

(defn ticker []
  (while true
    (Thread/sleep 200)
    (try
      (when out-fn
        (dosync (game/tick! out-fn)))
      (catch Exception ex
        (println ex)))))

(defn handle-events [msg]
  (in-handler/handle-event msg out-fn))

(defn start-websocket! []
  (def channel-socket
    (sente/make-channel-socket! http-kit/sente-web-server-adapter
                                {:user-id-fn #'random-uuid
                                 :csrf-token-fn nil
                                 :packer (json/->JsonTransitPacker)}))
  (def out-fn (partial out-handler/handle-event channel-socket)))

(defn start-router! []
  (def router
    (sente/start-chsk-router! (:ch-recv channel-socket) handle-events)))

(defn start-ticker! []
  (def ticker-thread
    (go (thread (ticker)))))

(defroutes routes
  (GET "/health" _ "OK")
  (GET  "/chsk" req ((:ajax-get-or-ws-handshake-fn channel-socket) req))
  (POST "/chsk" req ((:ajax-post-fn channel-socket) req))
  (route/resources "/")
  (route/not-found "Not found"))

(def handler
  (-> #'routes
      (r.reload/wrap-reload) ;TODO dev only
      (r.defaults/wrap-defaults (assoc-in r.defaults/site-defaults [:security :anti-forgery] false))
      (r.cors/wrap-cors :access-control-allow-origin [#".*"]
                        :access-control-allow-methods [:get :put :post :delete]
                        :access-control-allow-credentials ["true"])))
