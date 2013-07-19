(ns froid.init-test
  (:use clojure.test)
  (:require [froid.init :as f]))

(deftest test-init-all
  (testing "Check the number of teams"
    (is (= 5 
           (count ((f/init-all 5 3) :teams)))))
  (testing "Check the drivers by teams"
    (is (= 3
           (count (second (first ((f/init-all 5 3) :teams)))))))
  (testing "Check the number of drivers"
    (is (= 15
           (count ((f/init-all 5 3) :drivers)))))
  (testing "Verify that results are not always the same"
    (is (not= (f/init-all 5 2)
              (f/init-all 5 2))))
  (testing "Check that teams informations are coherent"
    (let [{teams :teams drivers :drivers} (f/init-all 5 3)
          first-team (first (first teams))
          first-driver (first (second (first teams)))]
      (is (= first-team
             (get (get drivers first-driver) :team))))))
