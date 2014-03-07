(ns sokoban2.handler
  (:use compojure.core
        sokoban2.levels
        [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.response :as response]
            [hiccup
             [page :refer [html5]]
             [page :refer [include-js]]]))

(defn index-page []
  (html5
    [:head
     [:title "GSokoban"]
     (include-js "/js/main.js")]
    [:body
     [:h1 "GSokoban"]
     ;[:canvas {:id "field" :width 300 :height 300}]
     [:a {:onclick "shit()" :href "#"} "llama"]
     (vec (concat [:table {:id "level"}]
      (let [lvl (sokoban2.levels/generate-level 11 11)]
        (vec
          (map (fn [xs]
                 (vec
                   (concat [:tr]
                           (map #(vector :td [:div (name %)]) xs))))
               lvl)))))]))

(defroutes app-routes
  (GET "/" [] (index-page))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (wrap-base-url)))
