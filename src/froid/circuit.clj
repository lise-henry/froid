(ns froid.circuit
  (:require [froid.ascii]
            [froid.gui])
  (:use [froid.core])
  (:import [java.lang.Thread]))



(defn random-circuit
  []
  """() -> circuit
     Generate a random circuit. A circuit is a map with
     the length, the ratios of straight lines, soft turns
     and high turns. Obviously, ratios should add to 1. """
     (let [length (rand-range 800 1200)
           straight (rand)
           soft (* (rand) (- 1 straight))
           hard (- 1 straight soft)
           overtakes (rand-range 1 5)]
       {:length length
        :straight straight
        :soft soft
        :hard hard
        :overtakes overtakes}))

(defn lap-time
  [driver circuit]
  """(Driver, circuit) -> double
     Computes the time a driver uses to run a lap on given circuit"""
     (let [length (:length circuit)]
       (-> (* (rand-froid (:speed-straight driver))
              (:straight circuit) 
              length)
           (+ (* (rand-froid (:speed-soft driver))
                 (:soft circuit) 
                 length))
           (+ (* (rand-froid (:speed-hard driver))
                 (:hard circuit) 
                 length)))))

(defn qual-time
  [driver circuit]
  """(Driver, circuit) -> Driver
     Returns the Driver with its lap time made during qualification"""
     (assoc driver :lap-time
            (* (lap-time driver circuit)
               (rand-froid (:skill-qual driver)))))

(defn new-time
  [driver circuit]
  """(Driver, circuit) -> Driver
     Return a version of the driver adjusted to the new time on the 
     circuit""" 
     (let [t (lap-time driver circuit)]
       (assoc driver 
         :lap-time t 
         :time (+ (:time driver) t))))

(defn try-overtake
  [drivers i]
  """(Drivers, int) -> Drivers
     i is the number of the driver trying to overtake the preceding one
     Returns an updated vector if overtake is succesful"""
     {:pre [(> 0 i)]}
     (let [d1 (drivers i)
           d0 (drivers (dec i))
           time-ratio (/ (:lap-time d1)
                         (:lap-time d0))
           take-ratio (* (rand-froid (:overtake d1))
                         (rand-froid (:block d0)))
           p (* time-ratio take-ratio)
           x (* (rand) 1.2)] ;; TODO: tune or set to config elsewhere
       (if (> x p)
         ;; overtake succesful, swap indices
         (do
           (println (:name d1) "overtakes" (:name d0))
           (assoc drivers (dec i) d1 i d0))
         ;; overtake failed, adjust second driver time
         (do
           (assoc drivers i 
                  (assoc d1 :time 
                       (+ (:time d0)
                          (/ 1.0 take-ratio))))))))

(defn try-overtakes
  [drivers]
  """Drivers -> Drivers
     Apply try-overtake above on a sorted list of drivers"""
     {:pre [(vector? drivers)]}
     (let [n (count drivers)]
       ;; loop to only swap positions when one driver
       ;; suceeds overtaking the preceding one
       (loop [v drivers
              i 1]
         (if (= i n)
           v
           (if (< (:time (drivers i))
                  (:time (drivers (dec i))))
             (recur (try-overtake v i) (inc i))
             (recur v (inc i)))))))
     
(defn time-step
  [drivers circuit]
  """(Drivers, circuit) -> Drivers
     Return an updated collection of drivers where time, 
     position... reflect having runned one more lap.
     drivers must a vector be sorted by position."""
     {:pre [(vector? drivers)]}
     ;; update time after this lap
     (let [drivers (vec (map #(new-time % circuit)
                        drivers))
           n (:overtakes circuit)]
       (loop [x 0
              drivers drivers]
         (if (= x n)
           drivers
           (recur (inc x) (try-overtakes drivers))))))

;;    


(defn plop
  [& args]
  (let [;;drivers (map froid.core/character->Driver (vals ((froid.init/init-all 5 3) :drivers)))
        characters (vals ((read-string (slurp "/tmp/miaou.clj")) :drivers))
        drivers (map froid.core/character->Driver characters)
        circuit (froid.circuit/random-circuit)
        drivers (sort-by :lap-time 
                         (map #(qual-time % circuit) 
                              drivers))
        frame (javax.swing.JFrame.)
        [panel update!] (froid.gui/create-gui characters)]
    (doto frame
      (.setDefaultCloseOperation javax.swing.JFrame/EXIT_ON_CLOSE)
      (.add panel)
      (.pack)
      (.setVisible true))
    (update! "Qualifications" drivers)
    (Thread/sleep 2000)
    (loop [drivers drivers
           l 0]
        (if (>= l 50)
          (update! "Results" drivers)
          (do
            (update! (str l) drivers)
            (froid.ascii/print-drivers drivers)
            (Thread/sleep 1000)
            (doseq [_ (range 1 4)] (println ""))
            (println (str "Lap: " l))
            (recur (froid.circuit/time-step drivers circuit) (inc l)))))))








;; now useless ???

'k@
(defn- assoc-range
  [vec [low high] value]
  """For vectors, like assoc, but with a range of indices"""
  (if (> low high)
    vec
    (recur (assoc vec low value) [(inc low) high] value)))


(defn evaluate-correctly?
  [driver]
 ' """Return true if driver is able to evaluate a pos correctly,
     false else"""
     (if (> (rand) (/ 0.1 (driver :driving)))
       true
       false))

(defn reasonable-speed
  [driver circuit pos]
  """Return the max viable (perceived) speed at a location on a circuit
     according to a driver's capacites"""
     (let [{adherence :adherence
            max-speed :max-speed} driver
           pos (mod pos (count circuit))
           speed (circuit pos)]
         (if (nil? speed)
           max-speed
           (+ speed adherence))))

(defn crash? 
  [driver circuit speed [low high]]
  """Return the first crash pos if the driver crashes between that set of pos, 
     false else"""
     (loop [x low]
       (if (> x high)
         false
         (if (> speed (reasonable-speed driver circuit x))
           (do 
             x)
           (recur (inc x))))))

(defn evaluate-crash
  [driver circuit speed [low high]]
  """Same as crash?, but not reliable"""
  (let [ret (crash? driver circuit speed [low high])]
    (if (evaluate-correctly? driver)
      ret
      (not ret))))
      
(defn reasonable-perceived-speed
  [driver circuit pos]
  """Return the reasonable speed, plus a random bias of the observer"""
  (let [s (reasonable-speed driver circuit pos)]
    (if (evaluate-correctly? driver)
      s
      (rand-range (dec s) (inc s)))))

(defn adjust-speed
  [driver circuit]
  """Return an updated structure where the driver speed
    is adapted to the location on the circuit"""
    (let [{x :current-pos
           speed :current-speed
           adherence :adherence
           brake :brake
           acceleration :acceleration
           max-speed :max-speeed
           driving :driving
           pos :current-pos} driver
          anticipation (inc (int (/ speed brake)))
          x (inc x)]
      (loop [new-speed (+ speed acceleration)]
        (if (<= new-speed (- speed brake))
          (assoc driver :current-speed new-speed)
          ;; avoid crashing this turn... and following
          (if (or 
               (evaluate-crash driver circuit new-speed
                               [(inc pos)
                                (+ pos new-speed)])
               (evaluate-crash driver circuit (- new-speed brake)
                               [(+ pos new-speed)
                                (- (+ pos new-speed new-speed)
                                   brake)]))
            (recur (dec new-speed))
            (assoc driver :current-speed new-speed))))))

(defn manage-crash
  [driver]
  """See if a driver avoids losing HP or not"""
  (if (> (rand) (/ 1.0 (driver :driving)))
    (do 
      (println (str (driver :name) " avoided crash"))
      driver)
    (do
      (println (driver :name) "crashes")
      (-> driver
          (assoc :hp (dec (driver :hp)))
          (assoc :current-speed 0)))))
