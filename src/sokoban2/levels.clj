(ns sokoban2.levels
  (:use sokoban2.search))
;  (:use clojure.core.matrix)
;  (:use clojure.core.matrix.operators))

(defn height [level]
  (count level))

(defn width [level]
  (count (first level)))

(defn vectorize2d
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
  (and (>= x 0)
       (>= y 0)
       (< x (width level))
       (< y (height level))
       (= (get-in level [y x]) :w)))

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

(defn summon-player
  "set player (:p) on the field near :c"
  [level]
  (assoc-in level (rand-neighbour level (rand-nth (find2d level :c))) :p))

(defn new-level-with-player
  "creates new level, populates it with completed goals and adds a player"
  [w h]
  (summon-player (set-completed-goals (new-level w h))))


(defn move
  [level from to]
  (let [path (sokoban2.search/search level from to :w)] ; the A* uses 0s and 1s to represent walls and free spaces
    (loop [field level [p & rp] path]
      (if (nil? p)
        field
        (recur (assoc-in field (reverse p) :e) rp)))))

;(defn move-boxes
;  [level [box & cboxes] pos]
;  (move-boxes level box cboxes pos))
