(ns grube-api.web
  (:require [compojure.core :refer [defroutes GET POST]]
            [environ.core :as environ]
            [ring.middleware.cors :as cors]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.reload :as reload]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [taoensso.timbre :refer [infof]]))

(declare channel-socket)

(defmulti event-handler :id)

(defmethod event-handler :default
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (infof "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-server event}))))

(defmethod event-handler :chsk/uidport-open [{:keys [uid client-id]}]
  (println "New connection:" uid client-id))

(defmethod event-handler :chsk/uidport-close [{:keys [uid]}]
  (println "Disconnected:" uid))

(defmethod event-handler :chsk/ws-ping [_])

(defn start-websocket []
  (defonce channel-socket
    (sente/make-channel-socket-server!
     (get-sch-adapter) {})))

(defn start-router []
  (defonce router
    (sente/start-chsk-router! (:ch-recv channel-socket) event-handler)))

(defroutes app-routes
  (GET "/health" _ "Ok")
  (GET "/chsk" req ((:ajax-get-or-ws-handshake-fn channel-socket) req))
  (POST "/chsk" req ((:ajax-post-fn channel-socket) req)))

(def app
  (-> #'app-routes
      (cond-> (environ/env :dev?) (reload/wrap-reload))
      (defaults/wrap-defaults (assoc-in defaults/site-defaults [:security :anti-forgery] false))
      (cors/wrap-cors :access-control-allow-origin [#".*"]
                      :access-control-allow-methods [:get :put :post :delete]
                      :access-control-allow-credentials ["true"])))
