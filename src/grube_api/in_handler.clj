(ns grube-api.in-handler
  (:require [clojure.walk :as walk]
            [grube-api.game :as game]))

(defmulti in (fn [msg _ _] (:id msg)))

(defmethod in :default
  [{:keys [event]} _ _]
  (println (str "Unhandled in event: " event)))

(defmethod in :chsk/uidport-open
  [{:keys [uid client-id]} _ out-fn]
  (println "New connection:" uid client-id)
  (game/add-new-player! uid out-fn))

(defmethod in :chsk/uidport-close
  [{:keys [uid]} _ out-fn]
  (println "Disconnected:" uid)
  (game/remove-player! uid out-fn))

(defmethod in :grube/move-player
  [{:keys [uid]} data out-fn]
  (let [position (select-keys data [:x :y])
        direction (:direction data)]
    (game/move-player! uid direction position out-fn)))

(defmethod in :grube/player-shoot
  [{:keys [uid]} data out-fn]
  (let [position (select-keys data [:x :y])
        direction (:direction data)]
    (game/player-shoot! uid direction position out-fn)))

(defmethod in :grube/player-respawn
  [{:keys [uid]} _ out-fn]
  (game/player-respawn! uid out-fn))

(defn handle-event
  [{:keys [?data] :as msg} out-fn]
  (in msg (walk/keywordize-keys ?data) out-fn))
