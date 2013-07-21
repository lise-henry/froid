(ns froid.circuit
  (:require [froid.ascii])
  (:use [froid.core]
        [froid.config])
  (:import [java.lang.Thread]))

(defn random-circuit
  "() -> circuit
   Generate a random circuit. A circuit is a map with
   the length, the ratios of straight lines, soft turns
   and high turns. Obviously, ratios should add to 1. "
  []
  (let [length (rand-range 800 1200)
        straight (rand)
        soft (rand)
        hard (rand)
        total (+ straight soft hard)
        straight (/ straight total)
        soft (/ soft  total)
        hard (/ hard total)
        overtakes (rand-range 1 5)
        security (rand-range 50 100)]
    {:length length
     :straight straight
     :soft soft
     :hard hard
     :overtakes overtakes
     :security security}))

(defn lap-time
  "(Driver, circuit) -> double
   Computes the time a driver uses to run a lap on given circuit"
  [driver circuit]
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
  "(Driver, circuit) -> Driver
   Returns the Driver with its lap time made during qualification"
  [driver circuit]
  (assoc driver :lap-time
         (* (lap-time driver circuit)
            (rand-froid (:skill-qual driver)))))

(defn new-time
  "(Driver, circuit) -> Driver
   Return a version of the driver adjusted to the new time on the 
   circuit"
  [driver circuit]
  (let [t (lap-time driver circuit)]
    (assoc driver 
      :lap-time t 
      :time (+ (:time driver) t))))

(defn test-crash
  "(Driver, circuit) -> Driver
   Updates :alive field if driver crashes"
  [driver circuit]
  {:pre [(:alive? driver)]}
  (let [p (* (/ 1.0 (:security circuit))
             (:skill-crash driver))
        x (rand)]
    (if (< x p)
      (do
        (println (:name driver) "crashed!")
        (assoc driver :alive? false))
      driver)))
                
             

(defn try-overtake
  "(Drivers, int) -> Drivers
   i is the number of the driver trying to overtake the preceding one
   Returns an updated vector if overtake is succesful"
  [drivers i]
  {:pre [(> i 0)]}
  (let [d1 (drivers i)
        d0 (drivers (dec i))
        time-ratio (/ (:lap-time d1)
                      (:lap-time d0))
        take-ratio (* (rand-froid (:overtake d1))
                      (rand-froid (:block d0)))
        p (* time-ratio take-ratio)
        x (* (rand) 1.0)] ;; TODO: tune or set to config elsewhere
    (if (or (> x p)
            (= (:team d0) (:team d1)))
      ;; overtake succesful, swap indices
      (do

        (println (:name d1) "overtakes" (:name d0))
        (assoc drivers (dec i) d1 i d0))
      ;; overtake failed, adjust second driver time
      (do
        (assoc drivers i 
               (assoc d1 :time 
                      (+ (:time d0)
                         (* 10 take-ratio))))))))

(defn try-overtakes
  "Drivers -> Drivers
   Apply try-overtake above on a sorted list of drivers"
  [drivers]
  {:pre [(vector? drivers) (> (count drivers) 0)]}
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
  "(Drivers, circuit) -> Drivers
   Return an updated collection of drivers where time, 
   position... reflect having runned one more lap.
   drivers must a vector be sorted by position."
  [drivers circuit]
  ;; update time after this lap
  (let [drivers (vec (map #(new-time % circuit)
                          drivers))
        drivers (vec (filter :alive?
                             (map #(test-crash % circuit)
                                  drivers)))
        n (:overtakes circuit)]
    (println drivers)
    (loop [x 0
           drivers drivers]
      (if (= x n)
        drivers
        (recur (inc x) (try-overtakes drivers))))))

(defn post-race
  "(Drivers, data) -> data
   Returns updated character stats (such as points, victories, xp)"
  [drivers data]
  {:pre [(vector? drivers)]}
  (let [characters (:drivers data)
        n (count drivers)]
    (loop [x 0
           characters characters]
      (if (= x n)
        (assoc data :drivers characters)
        (let [name (:name (drivers x))
              character (characters name)
              pts (nth points-winned x 0)
              xp  (nth xp-winned x 0)]
          (recur (inc x)
                 (assoc characters
                        name
                        (-> character
                            (update-in [:points] + pts)
                            (update-in [:xp] + xp)
                            raise-stats-random))))))))
