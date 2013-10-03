(ns sokoban2.search
  (:use [clojure.data.priority-map]))

; A* taken from http://nakkaya.com/2010/06/01/path-finding-using-astar-in-clojure/
; #######################################################################

(defn manhattan-distance [[x1 y1] [x2 y2]]
  (+ (Math/abs ^Integer (- x2 x1)) (Math/abs ^Integer (- y2 y1))))

(defn cost [curr start end]
  (let [g (manhattan-distance start curr)
        h (manhattan-distance curr end)
        f (+ g h)]
    [f g h]))

(defn edges ; this function is changed from the original in order to have only orthogonal movements
  ([map width height closed [x y]]
   (edges map width height closed [x y] 0))

  ([map width height closed [x y] allowed]
   (for [tx (range (- x 1) (+ x 2))
         ty (range (- y 1) (+ y 2))
         :when (and (>= tx 0)
                    (>= ty 0)
                    (or (= tx x) (= ty y))
                    (<= tx width)
                    (<= ty height)
                    (not= [x y] [tx ty])
                    (= (nth (nth map ty) tx) allowed) ;AAAA
                    (not (contains? closed [tx ty])))]
     [tx ty])))

(defn path [end parent closed]
  (reverse
   (loop [path [end parent]
          node (closed parent)]
     (if (nil? node)
       path
       (recur (conj path node) (closed node))))))

(defn search ; changed a bit in order to be able to search path through specific type of squares
  ([map start end]
   (search map start end 0))

  ([map start end allowed]
     (let [[sx sy] start
           [ex ey] end
           open (priority-map-by
                 (fn [x y]
                   (if (= x y)
                     0
                     (let [[f1 _ h1] x
                           [f2 _ h2] y]
                       (if (= f1 f2)
                         (if (< h1 h2) -1 1)
                         (if (< f1 f2) -1 1)))))
                 start (cost start start end))
           closed {}
           width (-> map first count dec)
           height (-> map count dec)]
       (when (and (= (nth (nth map sy) sx) allowed) ;AAAA
                  (= (nth (nth map ey) ex) allowed)) ;AAAA
         (search map width height open closed start end allowed))))

  ([map width height open closed start end allowed]
     (if-let [[coord [_ _ _ parent]] (peek open)]
       (if-not (= coord end)
         (let [closed (assoc closed coord parent)
               edges (edges map width height closed coord allowed)
               open (reduce
                     (fn [open edge]
                       (if (not (contains? open edge))
                         (assoc open edge (conj (cost edge start end) coord))
                         (let [[_ pg] (open edge)
                               [nf ng nh] (cost edge start end)]
                           (if (< ng pg)
                             (assoc open edge (conj [nf ng nh] coord))
                             open))))
                     (pop open) edges)]
           (recur map width height open closed start end allowed))
         (path end parent closed)))))

(defn draw-map [area start end]
  (let [path (into #{} (time (search area start end)))
        area (map-indexed
              (fn [idx-row row]
                (map-indexed
                 (fn [idx-col col]
                   (cond (contains? path [idx-col idx-row]) \X
                         (= 1 col) \#
                         :default \space))
                 row))
              area)]
    (doseq [line area]
      (println line))))

; ##################################################################
; end of A*
