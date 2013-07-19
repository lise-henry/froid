(ns froid.main
  (:require [froid.ascii]
            [froid.init]
            [froid.circuit])
;;            [java.lang.Thread])
  (:gen-class))

     


(defn -main
  "Entry point of the program"
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (froid.circuit/plop))
;;   (let [data (froid.init/init-all 5 3)
;;         circuit (froid.circuit/random-circuit)]
;;     (froid.ascii/print-teams data)
;;     (loop [drivers (vals (data :drivers))
;;            t 0]
;;       (if (>= ((first drivers) :current-pos)
;;               (count circuit))
;;         0
;;         (do
;; ;;          (java.lang.Thread/sleep 1000)
;;           (doseq [_ (range 1 4)] (println ""))
;;           (println (str "Time: " t))
;;           (recur (froid.circuit/time-step drivers circuit) (inc t)))))))
          