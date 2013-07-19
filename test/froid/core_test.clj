(ns froid.core-test
  (:use clojure.test)
  (:require [froid.init :as f]))

(deftest dumb-test
  (testing "2 + 2 = 4 ?"
    (is (= 4
           (+ 2 2)))))