(ns sokoban2.levels
  (:use [sokoban2.search]))

(defn- height [level]
  (count level))

(defn- width [level]
  (count (first level)))

(defn- vectorize2d
  "converts all subseqs and the seq to vectors"
  [coll]
  (vec (map vec coll)))

(defn new-level
  "creates new level filled with walls"
  [w h]
  (vectorize2d (partition w (take (* w h) (repeat :w)))))

(defn needed-goals-count
  "returns the number of needed goals for a level"
  [level]
  (+ (/ (* (height level) (width level))
        15)
     (inc (rand-int 3))))

(defn set-completed-goals
  "sets completed goals to a level (goals with boxes on them) in order
   to generate new level"
  ([level]
   (set-completed-goals
     level
     (take
       (needed-goals-count level)
       (repeatedly #(vector (rand-int (height level)) (rand-int (width level)))))))
  ([level [pos & wall-positions]]
   (if (empty? wall-positions)
     level
     (set-completed-goals (assoc-in level pos :c) wall-positions))))

(defn neighbours
  "return all neighbours of position"
  [[x y]]
  (let [n [[-1 0] [1 0] [0 -1] [0 1]]]
    (map
      #(vector (+ x (first %)) (+ y (second %)))
      n)))

(defn valid-neighbour?
  "checks if neighbour is valid (in the borders)"
  [level [x y]]
  (and (> x 0) (> y 0) (< x (width level)) (< y (height level))))

(defn valid-neighbours
  "returns only the valid (in the borders of the matrix)"
  [level pos]
  (filter (partial valid-neighbour? level) (neighbours pos)))

(defn rand-neighbour
  "returns random neighbour of pos"
  [level pos]
  (rand-nth (valid-neighbours level pos)))

(defn find2d
  "returns all coordinates of occurances of item"
  [data item]
  (for [[x row] (map-indexed vector data)
        [y val] (map-indexed vector row)
        :when (= item val)] [x y]))

;TODO fix this
(defn summon-player
  "set player (:p) on the field"
  [level]
  (loop [pos [(rand-int (height level)) (rand-int (width level))]]
    (if (= (get-in level pos) :w)
      (assoc-in level pos :p)
      (recur [(rand-int (height level)) (rand-int (width level))]))))


(defn move
  [level from to]
  (let [path (sokoban2.search/search level from to)]
    (loop [field level [p & rp] path]
      (if (empty? rp)
        field
        (recur (assoc-in field (reverse p) 0) rp)))))

;(defn move-boxes
;  [level [box & cboxes] pos]
;  (move-boxes level box cboxes pos))
