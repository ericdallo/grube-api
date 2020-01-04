(ns grube-api.web
  (:require [clojure.core.async :refer [go thread]]
            [clojure.data.json :as json]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [grube-api.game :as game]
            [grube-api.handler :as handler]
            [ring.middleware.cors :as r.cors]
            [ring.middleware.defaults :as r.defaults]
            [ring.middleware.reload :as r.reload]
            [taoensso.sente :as sente]
            [taoensso.sente.interfaces :as sente.interfaces]
            [taoensso.sente.server-adapters.http-kit :as http-kit])
  (:import java.util.UUID))

(declare channel-socket)

(defn ^:private keywordize
  [value]
  (if (string? value)
    (keyword value)
    value))

(deftype JsonTransitPacker []
  sente.interfaces/IPacker
  (pack   [_ object] (json/write-str object))
  (unpack [_ string] (->> string
                          json/read-str
                          (map keywordize)
                          vec)))

(defn uid [_]
  (str (UUID/randomUUID)))

(defn broadcast []
  (let [all-users @(:connected-uids channel-socket)
        send-fn!  (:send-fn channel-socket)]
    (doseq [user-id (:any all-users)]
      (send-fn! user-id [:game/world @game/world]))))

(defn ticker []
  (while true
    (Thread/sleep 10)
    (try
      (dosync (game/tick))
      #_(broadcast)
      (catch Exception ex
        (println ex)))))

(defn handle-broadicasting [msg]
  (handler/handle-event msg)
  (broadcast))

(defn start-websocket! []
  (def channel-socket
    (sente/make-channel-socket! http-kit/sente-web-server-adapter
                                {:user-id-fn #'uid
                                 :csrf-token-fn nil
                                 :packer (->JsonTransitPacker)})))

(defn start-router! []
  (def router
    (sente/start-chsk-router! (:ch-recv channel-socket) handle-broadicasting)))

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
