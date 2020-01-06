(ns grube-api.game
  (:import [java.util UUID])
  (:require [grube-api.player :as player]
            [grube-api.bullet :as bullet]
            [clojure.pprint :as p]))

(defonce world (atom {:size {:width 9.0
                             :height 16.0}
                      :players {}
                      :bullets {}}))

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
  [{:keys [bullets] :as world}
   player-id
   direction
   position]
  (let [new-bullet         (bullet/new-bullet direction position)
        current-bullets    (or (get bullets player-id) [])
        new-player-bullets (conj current-bullets new-bullet)
        bullets            (-> bullets
                               (assoc player-id new-player-bullets))]
    (assoc world :bullets bullets)))

(defn ^:private move-player-bullets
  [[player-id bullets]]
  (let [moved-bullets (map bullet/move bullets)]
    {player-id moved-bullets}))

(defn ^:private move-all-bullets*
  [{:keys [bullets] :as world}]
  (let [moved-bullets (map move-player-bullets bullets)]
    (assoc world :bullets (into {} moved-bullets))))

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
  (swap! world player-shoot* player-id direction position))
