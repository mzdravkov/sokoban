(ns sokoban2.handler
  (:use compojure.core
        sokoban2.levels
        [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.response :as response]
            [hiccup
             [page :refer [html5]]
             [page :refer [include-js include-css]]]))

(defn get-level
  [w h]
  (letfn [(lvl-or-false []
            (deref (future (sokoban2.levels/generate-level w h))
                           3000
                           false))]
  (loop [lvl (lvl-or-false)]
    (if lvl
      (if (sokoban2.levels/find2d lvl :p)
        lvl
        (recur (lvl-or-false)))
      (recur (lvl-or-false))))))

(def current-level (agent (get-level 11 11)))

(defn next-level [_ w h]
  (get-level w h))

(defn now [] (.getTime (java.util.Date.)))

(def lvl-time (ref (now)))
(def in-game? (agent false))

(future (while true
          (Thread/sleep 30000)
          (send current-level next-level 11 11)
          (dosync (ref-set lvl-time (now)))
          (send in-game? not)))

(defn status-page []
  (html5
    [:head
     [:title "GSokoban"]]
    [:body "llama"]))

(defn level-time []
  (html5
    [:body
     [:div {:id "level-time"}
      (let [timer (- (now) @lvl-time)]
        (if @in-game?
          timer
          (- timer)))]]))

(defn index-page []
  (html5
    [:head
     [:title "GSokoban"]
     (include-js "/js/main.js")
     (include-css "css/main.css")]
    [:body
     [:h1 {:style "color: white;"} "SokoMexican"]
     (let [lvl @current-level;(get-level 11 11)
           width (sokoban2.levels/width lvl)
           height (sokoban2.levels/height lvl)
           ids (partition width
                          (for [x (range width)
                                y (range height)]
                            (str x "_" y)))]
       (vec
         (concat [:table {:id "level"}]
                 (sokoban2.levels/vectorize2d
                   (map (fn [row ids-row]
                          (concat [:tr]
                                  (map #(vector :td
                                                [:div {:id %2} (name %1)])
                                       row ids-row)))
                        lvl ids)))))
     [:input {:id "input" :size "0"}]
     [:script "start();"]]))

(defroutes app-routes
  (GET "/" [] (index-page))
  (GET "/status" [] (status-page))
  (GET "/level-time" [] (level-time))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (wrap-base-url)))
