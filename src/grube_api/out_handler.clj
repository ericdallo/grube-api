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

(defmethod out :player-paused
  [{:keys [player-id]}
   player-id-to-send]
  (when (not= player-id player-id-to-send)
    [:game/enemy-paused {:enemy-id player-id}]))

(defmethod out :player-resumed
  [{:keys [player-id]}
   player-id-to-send]
  (when (not= player-id player-id-to-send)
    [:game/enemy-resumed {:enemy-id player-id}]))

(defmethod out :player-moved
  [{:keys [player]} _]
  [:game/player-moved {:player player}])

(defmethod out :player-shot
  [{:keys [player-id bullets]} user-id]
  (if (= player-id user-id)
    [:game/player-shot {:player-id player-id
                        :bullets bullets}]
    [:game/enemy-shot {:enemy-id player-id
                       :bullets bullets}]))

(defmethod out :bullets-moved
  [{:keys [bullets-by-player]} _]
  [:game/bullets-moved {:bullets-by-player bullets-by-player}])

(defmethod out :players-hitted
  [{:keys [player-ids]} _]
  [:game/players-hitted {:player-ids player-ids}])

(defmethod out :players-scored
  [{:keys [players crowned-player]}
   player-id]
  (println crowned-player)
  (if-let [player (player/find-by-id players player-id)]
    [:game/player-scored {:score (:score player)
                          :crowned-player (:id crowned-player)}]
    (let [scored-enemies (map #(select-keys % [:id :score]) players)]
      [:game/enemies-scored {:enemies scored-enemies
                             :crowned-player (:id crowned-player)}])))

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
