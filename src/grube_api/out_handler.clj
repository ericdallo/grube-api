(ns grube-api.out-handler)

(defn ^:private send-to-all
  [socket out-result]
  (let [all-users @(:connected-uids socket)
        send-fn!  (:send-fn socket)]
    (doseq [user-id (:any all-users)]
      (send-fn! user-id out-result))))

(defmulti out :id)

(defmethod out :default
  [{:keys [id]}]
  (println (str "Unhandled out event: " id)))

(defmethod out :player-added
  [{:keys [player world]}]
  [:game/player-added {:player player
                       :world world}])

(defmethod out :player-removed
  [{:keys [player-id]}]
  [:game/player-removed {:player-id player-id}])

(defmethod out :player-moved
  [{:keys [player]}]
  [:game/player-moved {:player player}])

(defmethod out :bullets-moved
  [{:keys [bullets-by-player]}]
  [:game/bullets-moved {:bullets-by-player bullets-by-player}])

(defmethod out :players-hitted
  [{:keys [player-ids]}]
  [:game/players-hitted {:player-ids player-ids}])

(defn handle-event
  [socket event-id event-payload]
  (->> (assoc event-payload :id event-id)
       out
       (send-to-all socket)))
