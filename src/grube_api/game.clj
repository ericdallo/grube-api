(ns grube-api.game
  (:import [java.util UUID])
  (:require [grube-api.player :as player]))

(defonce game (atom {:players-count 0
                     :players {}}))

(defn ^:private add-player*
  [{:keys [players-count players]}
   {:keys [id] :as new-player}]
  {:players-count (inc players-count)
   :players (assoc players id new-player)})

(defn ^:private remove-player*
  [{:keys [players-count players]}
   player-id]
  {:players-count (dec players-count)
   :players (dissoc players player-id)})

(defn add-new-player [player-id]
  (let [new-player (player/new-player player-id)]
    (swap! game add-player* new-player)))

(defn remove-player [player-id]
  (swap! game remove-player* player-id))
