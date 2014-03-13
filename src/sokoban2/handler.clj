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

(defn index-page []
  (html5
    [:head
     [:title "GSokoban"]
     (include-js "/js/main.js")
     (include-css "css/main.css")]
    [:body
     [:h1 "GSokoban"]
     ;[:canvas {:id "field" :width 300 :height 300}]
     (let [lvl (sokoban2.levels/generate-level 11 11)
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
     [:script "shit();"]]))

(defroutes app-routes
  (GET "/" [] (index-page))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (wrap-base-url)))
