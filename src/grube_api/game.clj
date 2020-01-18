(ns grube-api.game
  (:require [clj-time.core :as t]
            [grube-api.bullet :as bullet]
            [grube-api.player :as player]))

(defonce world (atom {:size {:width 9.0
                             :height 16.0}
                      :players []}))

(defn ^:private add-player!*
  [{:keys [size players] :as world}
   player-id
   out-fn]
  (let [new-player (player/new-player player-id size)
        world (assoc world :players (conj players new-player))]
    (out-fn :player-added {:player new-player :world world})
    world))

(defn ^:private remove-player!*
  [{:keys [players] :as world}
   player-id
   out-fn]
  (let [updated-players (->> players
                             (remove #(= (:id %) player-id))
                             vec)
        world (assoc world :players updated-players)]
    (out-fn :player-removed {:player-id player-id})
    world))

(defn ^:private move-player!*
  [{:keys [players] :as world}
   player-id
   direction
   position
   out-fn]
  (let [player (-> players
                   (player/find-by-id player-id)
                   (assoc :position position)
                   (assoc :direction direction))
        world (assoc world :players (player/add-to-players players player))]
    (out-fn :player-moved {:player player})
    world))

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
    (-> player
        (assoc :bullets new-bullets)
        (assoc :shooting (and (empty? new-bullets)
                              (not (empty? bullets)))))))

(defn ^:private move-all-bullets!
  [{:keys [players size] :as world}
   out-fn]
  (let [players           (map (partial move-player-bullets!* size) players)
        world             (assoc world :players (vec players))
        bullets-by-player (into {} (map player/bullets-by-player-id players))]
    (when (not (empty? bullets-by-player))
      (out-fn :bullets-moved {:bullets-by-player bullets-by-player}))
    world))

(defn ^:private check-hitted-players!
  [{:keys [players] :as world}
   out-fn]
  (let [players           (map (partial player/hit-player players) players)
        hitted-player-ids (->> players
                               (filter (partial player/hitted? players))
                               (map (juxt :id))
                               (into [])
                               flatten)
        world             (assoc world :players (vec players))]
    (when (not (empty? hitted-player-ids))
      (out-fn :players-hitted {:player-ids hitted-player-ids}))
    world))

(defn ^:private score-players!
  [{:keys [players] :as world}
   out-fn]
  (let [players-to-score (filter (partial player/killed-other-player? players) players)
        players*         (map (partial player/score-if-contains players-to-score) players)
        world*           (assoc world :players players*)
        scored-players   (map player/score players-to-score)]
    (out-fn :players-scored {:players scored-players})
    world*))

(defn add-new-player! [player-id out-fn]
  (swap! world add-player!* player-id out-fn))

(defn remove-player! [player-id out-fn]
  (swap! world remove-player!* player-id out-fn))

(defn move-player!
  [player-id direction position out-fn]
  (swap! world move-player!* player-id direction position out-fn))

(defn player-shoot!
  [player-id direction position]
  (if-let [last-bullet (player/last-shot-bullet (:players @world) player-id)]
    (let [now                      (t/now)
          player                   (player/find-by-id (:players @world) player-id)
          stamina-time             (player/stamina->seconds player)
          last-bullet-plus-stamina (t/plus (:created-at last-bullet) stamina-time)]
      (when (t/after? now last-bullet-plus-stamina)
        (swap! world player-shoot!* player-id direction position now)))
    (swap! world player-shoot!* player-id direction position (t/now))))

(defn tick! [out-fn]
  (swap! world move-all-bullets! out-fn)
  (swap! world check-hitted-players! out-fn)
  (swap! world score-players! out-fn))
