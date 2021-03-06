(ns grube-api.player
  (:require [grube-api.bullet :as bullet]
            [clj-time.core :as t])
  (:import [java.util Random]))

(defn new-player [id {:keys [width height]}]
  (let [seed (hash id)
        random (Random. seed)
        random-x (.nextInt random width)
        random-y (.nextInt random height)]
    {:id id
     :life 3
     :score 0
     :stamina 10
     :direction :right
     :position {:x (double random-x)
                :y (double random-y)}
     :step 1.0
     :size 1.5
     :color 0xFF1ABC9C
     :bullets []}))

(defn find-by-id
  [players player-id]
  (->> players
       (filter #(= player-id (:id %)))
       first))

(defn merge-in-players
  [players updated-player]
  (-> (fn [player]
        (if (= (:id updated-player) (:id player))
            updated-player
            player))
      (map players)))

(defn bullets-by-player-id
  [{:keys [id bullets shooting]}]
  (when (or (not (empty? bullets))
             shooting)
    {id bullets}))

(defn stamina->seconds
  [player]
  (-> player
      :stamina
      (* 100)
      t/millis))

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

(defn hitted?
  [players
   {:keys [id position life]}]
  (when (>= life 0)
    (->> (enemies players id)
         (map #(:bullets %))
         flatten
         (map #(:position %))
         (some (partial bullet/hitted? position)))))

(defn killed-other-player?
  [players
   {:keys [id bullets]}]
  (let [dead-enemies-positions (->> (enemies players id)
                                    (filter #(= (:life %) 0))
                                    (map (juxt :position))
                                    flatten)
        bullets-positions (->> bullets
                               (map (juxt :position))
                               flatten)]
    (some #(some (partial bullet/hitted? %) dead-enemies-positions) bullets-positions)))

(defn score [player]
  (update-in player [:score] inc))

(defn score-if-contains
  [scored-players
   {:keys [id] :as player}]
  (if (some #(= (:id %) id) scored-players)
    (score player)
    player))

(defn find-crowned
  [players]
  (when (not (empty? players))
    (let [max-score-player       (apply max-key :score players)
          players-with-max-score (->> players
                                      (filter #(= (:score %) (:score max-score-player)))
                                      count)]
      (when (= players-with-max-score 1)
        max-score-player))))

(defn hit-player
  [players player]
  (if (hitted? players player)
    (update-in player [:life] dec)
    player))

(defn respawn [player new-position]
  (-> player
      (assoc :life 3)
      (assoc :position new-position)))
