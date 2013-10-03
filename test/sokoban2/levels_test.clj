(ns sokoban2.levels-test
  (:require [clojure.test :refer :all]
            [sokoban2.levels :refer :all]))

(deftest new-level-test
  (testing "creates new level filled with walls"
    (is (= [[:w :w :w]
            [:w :w :w]
            [:w :w :w]]
           (new-level 3 3 )))))

(deftest set-completed-goals-test
  (testing "add completed goals to a level (goals with boxes on them)"
    (is (> (count (filter #{:c} (flatten (set-completed-goals (new-level 5 5)))))
           0))))

(deftest summon-player-test
  (testing "add player to the field by putting him near a completed goal"
    (is (= false true))))
