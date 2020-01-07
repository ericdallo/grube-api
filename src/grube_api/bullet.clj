(ns grube-api.bullet)

(defn ^:private position-for-direction
  [direction position]
  (condp = direction
    "right" (update-in position [:x] inc)
    "left" (update-in position [:x] dec)
    "up" (update-in position [:y] dec)
    "down" (update-in position [:y] inc)))

(defn new-bullet
  [direction position as-of]
  {:direction direction
   :position position
   :created-at as-of})

(defn move
  [{:keys [direction position] :as bullet}]
  (let [new-position (position-for-direction direction position)]
    (assoc bullet :position new-position)))

(defn invalid-position?
  [{:keys [width height]}
   {{:keys [x y]} :position}]
  (or (> x width)
      (> y height)
      (< x 0)
      (< y 0)))
