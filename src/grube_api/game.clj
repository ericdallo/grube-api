(ns grube-api.game
  (:import [java.util UUID])
  (:require [grube-api.player :as player]))

(defonce world (atom {:size {:width 9.0
                             :height 16.0}
                      :players-count 0
                      :players {}}))

(defn tick []
  #_(prn "tick"))

(defn ^:private add-player*
  [{:keys [players-count players] :as world}
   {:keys [id] :as new-player}]
  (merge world
         {:players-count (inc players-count)
          :players (assoc players id new-player)}))

(defn ^:private remove-player*
  [{:keys [players-count players] :as world}
   player-id]
  (merge world
         {:players-count (dec players-count)
          :players (dissoc players player-id)}))

(defn add-new-player [player-id]
  (let [new-player (player/new-player player-id)]
    (swap! world add-player* new-player)))

(defn remove-player [player-id]
  (swap! world remove-player* player-id))

(defn move-player*
  [{:keys [players-count players] :as world}
   player-id
   position]
  (merge world
         {:players-count players-count
          :players (assoc-in players [player-id :position] position)}))

(defn move-player
  [player-id position]
  (swap! world move-player* player-id position))
