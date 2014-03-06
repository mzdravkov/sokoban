(ns sokoban2.levels
  (:use sokoban2.search))

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
        (+ (height level) (width level)))
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
       (or (= (get-in level [y x]) :w)
           (= (get-in level [y x]) :e))))

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
  (let [path (sokoban2.search/search from to level)]
    (loop [field level [p & rp] path]
      (if (nil? p)
        field
        (recur (assoc-in field (reverse p) :e) rp)))))

(defn move-something [level [from-x from-y] [to-x to-y] something]
  (-> (assoc-in level [from-y from-x] :e)
      (move [from-x from-y] [to-x to-y])
      (assoc-in [to-y to-x] something)))

(defn rand-direction []
  (let [direction [0 (rand-nth [-1 1])]]
    (if (zero? (rand-int 2))
      direction
      (vec (reverse direction)))))

(defn rand-valid-direction [field [px py] [bx by]]
  (first
    (filter #(and (valid-neighbour? field [(+ bx (first %) (first %)) (+ by (second %) (second %))]) ; check if person can go to pull the box
                  (valid-neighbour? field [(+ bx (first %)) (+ by (second %))])) ; check the destination of the box
            (repeatedly rand-direction))))

(defn move-box [[player-x player-y] [box-x box-y] level]
    (loop [moves-count (rand-int (/ (* (height level) (width level)) (* 2 (/ (+ (height level) (width level)) 2))))
           bx box-x
           by box-y
           px player-x
           py player-y
           field level
           [dirx diry] (rand-valid-direction field [px py] [bx by])]
      (let [next-field (-> (move-something field [px py] [(+ bx dirx) (+ by diry)] :p)
                           (move-something [(+ bx dirx) (+ by diry)] [(+ bx dirx dirx) (+ by diry diry)] :p)
                           (move-something [bx by] [(+ bx dirx) (+ by diry)] :c))]
        (if (zero? moves-count)
          next-field
          (let [rvd (deref
                      (future (rand-valid-direction next-field [(+ bx dirx dirx) (+ by diry diry)] [(+ bx dirx) (+ by diry)]))
                      100
                      :none)]
            (if (= rvd :none)
              next-field
              (recur (dec moves-count) (+ bx dirx) (+ by diry) (+ bx dirx dirx) (+ by diry diry) next-field rvd)))))))

(defn move-boxes
  ([level]
   (move-boxes level (map reverse (find2d level :c)) (reverse (first (find2d level :p)))))
  ([level [[bx by] & boxes] [px py]]
   (let [new-level (move-box [px py] [bx by] level)]
     (if (empty? boxes)
       new-level
       (recur new-level boxes (reverse (first (find2d new-level :p))))))))

(defn generate-level [width height]
  (let [init-level (new-level-with-player width height)
        goals (find2d init-level :c)
        level (move-boxes init-level)]
    (reduce #(assoc-in %1 %2 (if (= :c (get %1 %2)) :gc :g)) level goals)))

(defn draw-map [lvl]
  (doseq [line (map (fn [xs]
                      (map #(case %
                              :e " "
                              :w "#"
                              :c "$"
                              :gc "v"
                              :go "8"
                              :g "?"
                              :p "o")
                           xs))
                    lvl)]
    (println line)))
