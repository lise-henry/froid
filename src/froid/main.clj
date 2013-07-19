(ns froid.main
  (:require [froid.gui])
  (:gen-class))

     


(defn -main
  "Entry point of the program"
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (froid.gui/gui-main))