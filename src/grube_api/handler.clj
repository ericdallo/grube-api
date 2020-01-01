(ns grube-api.handler
  (:require [clojure.walk :as walk]
            [grube-api.game :as game]))

(defmulti event :id)

(defmethod event :default
  [{:keys [event]} _]
  (println (str "Unhandled event: " event)))

(defmethod event :chsk/uidport-open
  [{:keys [uid client-id]} _]
  (println "New connection:" uid client-id)
  (game/add-new-player uid))

(defmethod event :chsk/uidport-close
  [{:keys [uid]} _]
  (println "Disconnected:" uid)
  (game/remove-player uid))

(defmethod event :grube/test
  [_ data]
  (println (str "Received data: " data)))

(defn handle-event
  [{:keys [?data] :as msg}]
  (event msg (walk/keywordize-keys ?data)))
