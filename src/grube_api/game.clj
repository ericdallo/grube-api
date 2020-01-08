(ns grube-api.game
  (:import [java.util UUID])
  (:require [grube-api.player :as player]
            [grube-api.bullet :as bullet]
            [clj-time.core :as t]))

(defonce world (atom {:size {:width 9.0
                             :height 16.0}
                      :players {}}))

(defn ^:private add-player*
  [{:keys [players] :as world}
   {:keys [id] :as new-player}]
  (assoc world :players (assoc players id new-player)))

(defn ^:private remove-player*
  [{:keys [players] :as world}
   player-id]
  (assoc world :players (dissoc players player-id)))

(defn ^:private move-player*
  [{:keys [players] :as world}
   player-id
   direction
   position]
  (let [players (-> players
                    (assoc-in [player-id :position] position)
                    (assoc-in [player-id :direction] direction))]
    (assoc world :players players)))

(defn ^:private player-shoot*
  [{:keys [players] :as world}
   player-id
   direction
   position
   as-of]
  (let [new-bullet         (bullet/new-bullet direction position as-of)
        current-bullets    (get-in players [player-id :bullets])
        new-player-bullets (conj current-bullets new-bullet)]
    (assoc-in world [:players player-id :bullets] new-player-bullets)))

(defn ^:private move-player-bullets
  [world-size [player-id {:keys [bullets] :as player}]]
  (let [new-bullets (->> bullets
                         (map bullet/move)
                         (remove (partial bullet/invalid-position? world-size)))]
    {player-id (assoc player :bullets new-bullets)}))

(defn ^:private move-all-bullets*
  [{:keys [players size] :as world}]
  (let [players (map (partial move-player-bullets size) players)]
    (assoc world :players (into {} players))))

(defn tick []
  (swap! world move-all-bullets*))

(defn add-new-player [player-id]
  (let [new-player (player/new-player player-id)]
    (swap! world add-player* new-player)))

(defn remove-player [player-id]
  (swap! world remove-player* player-id))

(defn move-player
  [player-id direction position]
  (swap! world move-player* player-id direction position))

(defn player-shoot
  [player-id direction position]
  (if-let [last-bullet (player/last-shot-bullet @world player-id)]
    (let [now (t/now)
          last-bullet-plus-1-second (t/plus (:created-at last-bullet) (t/seconds 1))]
      (when (t/after? now last-bullet-plus-1-second)
        (swap! world player-shoot* player-id direction position now)))
    (swap! world player-shoot* player-id direction position (t/now))))
