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
  (game/add-new-player! uid))

(defmethod event :chsk/uidport-close
  [{:keys [uid]} _]
  (println "Disconnected:" uid)
  (game/remove-player! uid))

(defmethod event :grube/move-player
  [{:keys [uid]} data]
  (let [position {:x (:x data)
                  :y (:y data)}
        direction (:direction data)]
    (game/move-player! uid direction position)))

(defmethod event :grube/player-shoot
  [{:keys [uid]} data]
  (let [position {:x (:x data)
                  :y (:y data)}
        direction (:direction data)]
    (game/player-shoot! uid direction position)))

(defn handle-event
  [{:keys [?data] :as msg}]
  (event msg (walk/keywordize-keys ?data)))
