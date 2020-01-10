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

(defn find-by-id
  [players player-id]
  (->> players
       (filter #(= player-id (:id %)))
       first))

(defn add-to-players
  [players player-to-add]
  (-> (fn [player]
        (if (= (:id player-to-add) (:id player))
            player-to-add
            player))
      (map players)))

(defn last-shot-bullet
  [players player-id]
  (->> player-id
       (find-by-id players)
       :bullets
       (sort-by :created-at)
       last))

(defn enemies
  [players player-id]
  (->> players
       (remove (fn [{:keys [id]}] (= id player-id)))))

(defn ^:private player-hitted?
  [players
   {:keys [id position]}]
  (->> (enemies players id)
       (map #(:bullets %))
       flatten
       (map #(:position %))
       (some (partial bullet/hitted? position))))

(defn hit-player
  [players player]
  (if (player-hitted? players player)
    (update-in player [:life] dec)
    player))
