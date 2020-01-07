(ns grube-api.player)

(defn new-player [id]
  {:id id
   :direction :right
   :position {:x 0.0
              :y 0.0}
   :step 1.0
   :size 1.5
   :color 0xFF1ABC9C})

(defn last-shot-bullet
  [world player-id]
  (some->> [:bullets player-id]
           (get-in world)
           (sort-by :created-at)
           last))
