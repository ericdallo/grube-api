(ns grube-api.game
  (:require [clj-time.core :as t]
            [grube-api.bullet :as bullet]
            [grube-api.player :as player]))

(defonce world (atom {:size {:width 9.0
                             :height 16.0}
                      :players []}))

(defn ^:private add-player!*
  [{:keys [players] :as world}
   new-player]
  (assoc world :players (conj players new-player)))

(defn ^:private remove-player!*
  [{:keys [players] :as world}
   player-id]
  (let [updated-players (->> players
                             (remove #(= (:id %) player-id))
                             vec)]
    (assoc world :players updated-players)))

(defn ^:private move-player!*
  [{:keys [players] :as world}
   player-id
   direction
   position]
  (let [player (-> players
                   (player/find-by-id player-id)
                   (assoc :position position)
                   (assoc :direction direction))]
    (assoc world :players (player/add-to-players players player))))

(defn ^:private player-shoot!*
  [{:keys [players] :as world}
   player-id
   direction
   position
   as-of]
  (let [player             (player/find-by-id players player-id)
        new-bullet         (bullet/new-bullet direction position as-of)
        bullets            (conj (:bullets player) new-bullet)
        new-player-bullets (assoc player :bullets bullets)]
    (assoc world :players (player/add-to-players players new-player-bullets))))

(defn ^:private move-player-bullets!*
  [world-size {:keys [bullets] :as player}]
  (let [new-bullets (->> bullets
                         (map bullet/move)
                         (remove (partial bullet/invalid-position? world-size)))]
    (assoc player :bullets new-bullets)))

(defn ^:private move-all-bullets!
  [{:keys [players size] :as world}]
  (let [players (map (partial move-player-bullets!* size) players)]
    (assoc world :players (vec players))))

(defn ^:private check-hitted-players!
  [{:keys [players] :as world}]
  (let [players (map (partial player/hit-player players) players)]
    (assoc world :players (vec players))))

(defn add-new-player! [player-id]
  (let [new-player (player/new-player player-id)]
    (swap! world add-player!* new-player)))

(defn remove-player! [player-id]
  (swap! world remove-player!* player-id))

(defn move-player!
  [player-id direction position]
  (swap! world move-player!* player-id direction position))

(defn player-shoot!
  [player-id direction position]
  (if-let [last-bullet (player/last-shot-bullet (:players @world) player-id)]
    (let [now (t/now)
          last-bullet-plus-1-second (t/plus (:created-at last-bullet) (t/seconds 1))]
      (when (t/after? now last-bullet-plus-1-second)
        (swap! world player-shoot!* player-id direction position now)))
    (swap! world player-shoot!* player-id direction position (t/now))))

(defn tick! []
  (swap! world move-all-bullets!)
  (swap! world check-hitted-players!))
