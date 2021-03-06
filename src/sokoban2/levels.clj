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

(defn set-completed-goals
  "sets completed goals to a level (goals with boxes on them) in order
   to generate new level"
  ([level]
   (set-completed-goals
     level
     (take
       (needed-goals-count level)
       (repeatedly #(vector (rand-int (height level)) (rand-int (width level)))))))
  ([level [pos & more]]
   (if (empty? more)
     level
     (let [rand-neighbour #(rand-nth (valid-neighbours level (rand-nth more)))]
       (set-completed-goals (assoc-in level (rand-nth (take 4 (repeatedly rand-neighbour))) :c) more)))))

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

(defn valid-direction? [field [px py] [bx by] [dirx diry]]
  (and (valid-neighbour? field [(+ bx dirx dirx) (+ by diry diry)]) ; check if person can go to pull the box
       (valid-neighbour? field [(+ bx dirx) (+ by diry)]))) ; check the destination of the box

(defn rand-valid-direction [field [px py] [bx by]]
  (first
    (filter #(valid-direction? field [px py] [bx by] %)
            (repeatedly rand-direction))))

(defn move-box [[player-x player-y] [box-x box-y] level]
    (loop [moves-count (rand-int (/ (* (height level) (width level)) (+ (height level) (width level))))
           bx box-x
           by box-y
           px player-x
           py player-y
           field level
           [dirx diry] (rand-valid-direction field [px py] [bx by])
           last-rvd :none]
      (let [next-field (-> (move-something field [px py] [(+ bx dirx) (+ by diry)] :p)
                           (move-something [(+ bx dirx) (+ by diry)] [(+ bx dirx dirx) (+ by diry diry)] :p)
                           (move-something [bx by] [(+ bx dirx) (+ by diry)] :c))]
        (if (zero? moves-count)
          [next-field [(+ bx dirx) (+ by diry)]]
          (let [new-rvd (deref
                      (future (rand-valid-direction next-field [(+ bx dirx dirx) (+ by diry diry)] [(+ bx dirx) (+ by diry)]))
                      100
                      :none)
                rvd (if (or (= last-rvd :none)
                            (not (valid-direction? next-field [(+ bx dirx dirx) (+ by diry diry)] [(+ bx dirx) (+ by diry)] last-rvd)))
                      new-rvd
                      (rand-nth (vector new-rvd last-rvd last-rvd last-rvd last-rvd last-rvd)))]
            (if (= rvd :none)
              [next-field [(+ bx dirx) (+ by diry)]]
              (recur (dec moves-count) (+ bx dirx) (+ by diry) (+ bx dirx dirx) (+ by diry diry) next-field rvd [dirx diry])))))))

(defn manhattan-distance [[x1 y1] [x2 y2]]
  (+ (Math/abs ^Integer (- x2 x1)) (Math/abs ^Integer (- y2 y1))))

(defn move-boxes
  ([level]
   (let [player (reverse (first (find2d level :p)))
         boxes  (map reverse (find2d level :c))
         ordered-boxes (sort-by (partial manhattan-distance player) boxes)]
   (move-boxes level ordered-boxes player)))
  ([level [[bx by] & boxes] [px py]]
   (let [[new-level new-box-pos] (move-box [px py] [bx by] level)]
     (if (empty? boxes)
       new-level
       (recur new-level (rand-nth
                          (vector boxes boxes boxes (sort-by (partial manhattan-distance [px py])
                                                             (conj boxes new-box-pos))))
                          (reverse (first (find2d new-level :p))))))))

(defn add-outer-walls [level]
  (let [w (+ (height level) 2)
        h (+ (width level) 2)
        hwall (vec (take w (repeat :w)))]
    (concat [hwall]
            (vec (map #(concat [:w] % [:w]) level))
            [hwall])))

(defn generate-level [width height]
  (let [init-level (new-level-with-player width height)
        goals (find2d init-level :c)
        level (move-boxes init-level)]
    (add-outer-walls (reduce #(assoc-in %1 %2 (cond (= :c (get-in %1 %2)) :gc
                                   (= :o (get-in %1 %2)) :go
                                   :else :g))
            level goals))))

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
