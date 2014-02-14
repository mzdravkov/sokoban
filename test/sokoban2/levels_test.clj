(ns sokoban2.levels-test
  (:require [clojure.test :refer :all]
            [sokoban2.levels :refer :all]))

(deftest height-test
  (testing "returns the height of the level"
    (is (= 3 (height [1 2 3])))))

(deftest width-test
  (testing "returns the width of the level"
    (is (= 2 (width [[1 2] [1 2]])))))

(deftest vectorize2d-test
  (testing "converts all subseqs and the seq to vectors"
    (is (= [[1 2 3] [1 2]] (vectorize2d '((1 2 3) (1 2)))))))

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

#_(deftest neighbours-test
  (testing "returns all neighbours of a position"
    (is (= []
           (neighbours [3 3])))))

(deftest summon-player-test
  (testing "add player to the field by putting him near a completed goal"
    (let [level-with-player (new-level-with-player 5 5)]
      (is
        (not-empty
          (filter #(= (get-in level-with-player %) :c)
                  (valid-neighbours level-with-player (first (find2d level-with-player :p)))))))))
