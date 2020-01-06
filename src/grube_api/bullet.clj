(ns grube-api.bullet)

(defn ^:private position-for-direction
  [direction position]
  (condp = direction
    "right" (update-in position [:x] inc)
    "left" (update-in position [:x] dec)
    "up" (update-in position [:y] dec)
    "down" (update-in position [:y] inc)))

(defn new-bullet
  [direction position]
  {:direction direction
   :position position})

(defn move
  [{:keys [direction position] :as bullet}]
  (let [new-position (position-for-direction direction position)]
    (assoc bullet :position new-position)))
