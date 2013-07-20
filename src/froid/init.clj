(ns froid.init
  (:require [froid.core]))

(def ^:private driver-first-names
  ;; list of first names for random generator
  #{"Kelly"
    "Anya"
    "John"
    "Marcus"
    "Lucia"
    "Tony"
    "Millia"
    "Alex"
    "Will"
    "Max"
    "Andrea"
    "Victoria"
    "Lisa"
    "Karl"
    "Jose"
    "Vitto"
    "Letty"
    "Dom"
    "Vincent"
    "Miranda"
    "Cassie"
    "Nina"})

(def ^:private team-first-names
  ;; list of names for random generator
  #{"Hell"
    "Bloody"
    "Crazy"
    "Inglorious"
    "Damned"
    "Unholy"
    "Rude"
    "Lethal"
    "Fire"
    "Urban"})

(def ^:private team-last-names
  ;; list of names for random generator 
  #{"Riders"
    "Warriors"
    "Bastards"
    "Skinheads"
    "Killers"
    "Hound"
    "Hunters"
    "Avengers"
    "Angels"
    "Devils"})

(defn teams-generator
  [n-teams n-drivers]
  """Generate a random list of n-teams teams, 
     each having n-drivers drivers"""
  (let [teams (map #(str %1 " " %2)
                             (take n-teams (shuffle team-first-names))
                             (take n-teams (shuffle team-last-names)))
        total-drivers (* n-teams n-drivers)
        drivers (take total-drivers (shuffle driver-first-names))]
    (reduce into {}
            (map #(array-map %1 %2) 
                 teams
                 (map vec (partition n-drivers drivers))))))

(def ^:private default-character
  ;; Template stats for default character
  {:speed-hard 1
   :speed-soft 1
   :speed-straight 1
   :overtake 1
   :block 1
   :skill-qual 1
   :skill-crash 1
   :xp 0
   :points 0
   :victories 0})

(defn random-character-generator
  [name team]
  """(string string) -> character
     Generate a random character, whose name and teams are given"""
  (-> default-character
      (assoc :name name)
      (assoc :team team)
      (assoc :xp (froid.core/rand-range 10 20))
      froid.core/raise-stats-random))


(defn basic-character-generator
  [name team]
  """(string string) -> character
     Generate a basic character to be used by player"""
  (-> default-character
      (assoc :name name)
      (assoc :team team)
      (assoc :xp (froid.core/rand-range 10 20))
      (assoc :player true)))


(defn drivers-generator-from-teams
  [teams player-team]
  """Randomly (except for player's team) generate a list of drivers from a team map 
     (as returned by teams-generator)"""
     (reduce into {}
             (for [[team drivers] teams]
               (if (= team player-team)
                 (map #(array-map % (basic-character-generator % team))
                      drivers)
                 (map #(array-map % (random-character-generator % team))
                      drivers)))))

