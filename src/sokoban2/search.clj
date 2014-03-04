(ns sokoban2.search)

(defn neighbours*
  "return all neighbours of position"
  [[x y]]
  (let [n [[-1 0] [1 0] [0 -1] [0 1]]]
    (map
      #(vector (+ x (first %)) (+ y (second %)))
      n)))

(defn neighbours-list [neighbours counter]
  (vec
   (map
    #(vector % (inc counter))
    neighbours)))

(defn valid? [yx lvl]
  (let [v (get-in lvl (reverse yx))]
    (or (= v :w)
        (= v :e)
        (= v :p))))

(defn compare-counters-by [f]
  (fn [[p1 c1] [p2 c2]]
    (if (< c1 c2)
      [p1 c1]
      [p2 c2])))

(defn unique-by [f xs]
  (vec
   (map
    #(reduce (compare-counters-by f) %)
    (vals (group-by #(first %) xs)))))

(defn find-path
  ([start end vect] (find-path start end vect [] start))
  ([start end vect path current]
     (let [curr-neighbours (neighbours* current)]
       (if-let [target (some #{end} curr-neighbours)]
         (conj path current target)
         (find-path start end vect (conj path current) (first (reduce
                                                               (compare-counters-by min)
                                                               (filter #(some #{(first %)} curr-neighbours) vect))))))))

(defn search
  ([start end level]
     (search start end level [[end 0]]))
  ([start end level vect]
     (loop [vect vect index 0 changed false]
       (let [[coord counter] (vect index)]
         (if (some #(= start (first %)) vect)
           (find-path start end vect)
           (let [nl (neighbours-list (neighbours* coord) counter)
                 fnl (filter #(valid? (first %) level) nl)]
             (let [new-vect (unique-by min (concat vect fnl))]
               (if (>= (inc index) (count new-vect))
                 (if-not changed
                   nil
                   (recur new-vect 0 false))
                 (recur new-vect (inc index) (if (= (count vect) (count new-vect))
                                               (if changed true false)
                                               true))))))))))
