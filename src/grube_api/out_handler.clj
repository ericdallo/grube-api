(ns grube-api.out-handler
  (:require [grube-api.player :as player]))

(defmulti out :id)

(defmethod out :default
  [_ {:keys [id]}]
  (println (str "Unhandled out event: " id)))

(defmethod out :player-added
  [{:keys [player world]} _]
  [:game/player-added {:player player
                       :world world}])

(defmethod out :player-removed
  [{:keys [player-id]} _]
  [:game/player-removed {:player-id player-id}])

(defmethod out :player-moved
  [{:keys [player]} _]
  [:game/player-moved {:player player}])

(defmethod out :bullets-moved
  [{:keys [bullets-by-player]} _]
  [:game/bullets-moved {:bullets-by-player bullets-by-player}])

(defmethod out :players-hitted
  [{:keys [player-ids]} _]
  [:game/players-hitted {:player-ids player-ids}])

(defmethod out :players-scored
  [{:keys [players]}
   player-id]
  (if-let [{:keys [score]} (player/find-by-id players player-id)]
    [:game/player-scored {:score score}]))

(defmethod out :player-respawned
  [{:keys [player]} _]
  [:game/player-respawned {:player player}])

(defn ^:private send-to-all
  [socket payload]
  (let [all-users @(:connected-uids socket)
        send-fn!  (:send-fn socket)]
    (doseq [user-id (:any all-users)]
      (if-let [out-result (out payload user-id)]
        (send-fn! user-id out-result)))))

(defn handle-event
  [socket event-id event-payload]
  (->> (assoc event-payload :id event-id)
       (send-to-all socket)))
