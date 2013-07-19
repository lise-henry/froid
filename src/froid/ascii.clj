(ns froid.ascii)

(defn print-teams
  [{teams :teams}]
  """Print all teams and drivers belonging to them"""
  (doseq [k (keys teams)]
    (println k)
    (doseq [d (teams k)]
      (println (str "\t" d)))))

;; (defn print-drivers
;;   [{drivers :drivers}]
;;   """Print all drivers and the team they belong to"""
;;   (doseq [d (vals drivers)]
;;     (println (str (d :name) " (" (d :team) ")"))))

(defn format-time
  [time]
  """double -> string
     Gives the impression that internal time structure is actually
     hour/minute/second"""
     (let [time (int (* time 10))
           [s cs] [(quot time 100)
                   (rem time 100)]
           [mn s] [(quot s 60)
                   (rem s 60)]]
       (str mn "'" s "\"" cs)))

(defn time-diff
  "(Driver, double) -> String
   Given a time and a best time, returns a string containing
   the difference with first driver"
  [time best]
     (let [t (/ (- time best) 10)]
       (if (zero? t)
         (format-time time)
         (str "+" (format-time t)))))
         

(defn print-drivers
  [drivers]
  """Print all drivers and the team they belong to"""
  (let [best-time (:time (first drivers))]
    (doseq [d drivers]
      (println 
       (str (:name d) " (" (:team d)") : " (time-diff d best-time))))))
