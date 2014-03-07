(ns sokoban2.hello)

(defn draw []
  (let [canvas (.getElementById js/document "field")]
    (if-not (nil? (.-getContext canvas))
            (let [context (.getContext canvas "2d")]
              (set! (.-fillStyle context) "rgb(150, 29, 28)")
              (.fillRect context 10, 10, 100, 100)
              nil)
            (js/alert "This pages uses HTML5 to render correctly. Be modern and stop using shitty browsers. Please."))))

(defn get-cell-values []
  (let [table (.getElementById js/document "level")]
    (.-rows table)))
    ;(for [r (.-rows table)
    ;      c (.-cells r)]
    ;  (.innerHTML c))))

(defn beautify-field []
  (let [level (.getElementById js/document "level")]
    ))
