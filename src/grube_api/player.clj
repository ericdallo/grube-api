(ns grube-api.player
  (:require [grube-api.bullet :as bullet]))

(defn new-player [id]
  {:id id
   :life 3
   :direction :right
   :position {:x 0.0
              :y 0.0}
   :step 1.0
   :size 1.5
   :color 0xFF1ABC9C
   :bullets []})

(defn last-shot-bullet
  [world player-id]
  (->> [:players player-id :bullets]
       (get-in world)
       (sort-by :created-at)
       last))

(defn enemies
  [players player-id]
  (->> players
       (remove (fn [{:keys [id]}] (= id player-id)))))

(defn ^:private player-hitted?
  [players
   {:keys [id position]}]
  (->> (enemies (vals players) id)
       (map #(:bullets %))
       flatten
       (map #(:position %))
       (some (partial bullet/hitted? position))))

(defn hit-player
  [players player]
  (if (player-hitted? players (val player))
    {(key player) (update-in (val player) [:life] dec)}
    player))
