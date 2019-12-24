(ns grube-api.player)

(defn new-player [id]
  {:id id
   :position {:x 0.0
              :y 0.0}
   :speed 2.0
   :size 1.5
   :color 0xFF1ABC9C})
