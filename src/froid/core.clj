(ns froid.core)

(defn set-data!
  "Map -> Map
   Save the data to memory and/or file"
  ;; TODO: very very very ugly
  [data]
  (spit "/tmp/miaou.clj" data)
  data)

(defn get-data
  "() -> Map
   Get the data from memory or file"
  ;; TODO: very ugly
  []
  (read-string (slurp "/tmp/miaou.clj")))

(defn rand-range
  [low high]
  {:pre [(number? low) (number? high)]}
  """(int int) -> int
     Returns a random int between low and high (included)"""
     (rand-nth (range low (inc high))))

(defn rand-gauss
  [mu sigma]
  {:pre [(number? mu) (number? sigma)]}
  """(double double) -> double
     Return a number using a gaussian random law.
     Mu is the mean, sigma the standard deviation"""
     (let [x1 (rand)
           x2 (rand)
           y (* (Math/sqrt (* -2 (Math/log x1)))
                (Math/cos (* 2 x2 Math/PI)))]
       (+ mu
          (* sigma y))))

(defn rand-gauss-relative
  [mu sigma]
  {:pre [(number? mu) (number? sigma)]}
  """(double double) -> double
     Return a number using a gaussian random law.
     Mu is the mean, sigma the relative standard deviation"""
     (rand-gauss mu (* mu sigma)))

;; noise level for everything, ie relative standard deviation when
;; using rand-gauss-relative
(def ^:private default-sigma 0.5)

(defn rand-froid
  [mu]
  """double -> double
     Returns a random number, using gaussian law with mean mu
     and default sigma"""
     {:pre [(number? mu)]}
     (rand-gauss-relative mu (* default-sigma
                                (Math/abs
                                 (- 1.0 mu)))))


;; Structure for driver
;; A driver is sligthly different from a character: driver
;; are to be used by functions, and thus its values are set
;; to be directly usable by algorithms, instead of being 
;; user-readable
(defrecord Driver [speed-hard
                   speed-soft
                   speed-straight
                   overtake
                   block
                   skill-qual
                   skill-crash
                   speed
                   time
                   lap-time
                   alive?])
                   
                   
(def character-driver-stats
  ;; map to converts stats to "machine-friendly" stats
  ;; (ie, only do extensive computation once)
  ;; used by character->Driver
  {:speed-straight #(Math/pow 0.98 %)
   :speed-soft #(Math/pow 0.98 %)
   :speed-hard #(Math/pow 0.98 %)
   :overtake #(Math/pow 0.95 %)
   :block #(Math/pow (/ 1.0 0.95) %)
   :skill-qual #(Math/pow 0.98 %)
   :skill-crash #(Math/pow 0.95 %)})

(def ^:private character-driver
  ;; same as previous, with name and team additional
  (into character-driver-stats
        {:time (constantly 0)
         :lap-time (constantly 0)
         :alive? (constantly true)
         :name identity
         :team identity}))

(defn character->Driver
  [character]
  """character -> Driver
     Transform a character (a map with user-readable stats) to
     a driver (roughly same stats, but easier used by algorithms)"""
     (map->Driver
      (reduce into {}
              (for [k (keys character-driver)]
                (let [f (k character-driver)
                      x (k character)]
                  (if f
                    {k (f x)}
                    {k x}))))))
         
(defn raise-stat
  [character stat]
  """(character, keyword) -> character
     Try to raise a stat, if there is enough XP"""
  (let [xp (get character :xp 0)
        value (character stat)
        cost 1]
    (if (or (nil? value)
            (nil? cost))
      (throw (java.lang.IllegalArgumentException. 
              (str "No such field exists: "
                   stat)))
      (if (>= xp cost)
        (-> character
            (assoc-in [:xp] (- xp cost))
            (assoc-in [stat] (inc value)))
        character))))
      
(defn raise-stats-random
  "character -> character
   Raise stats at random for IA while there is XP"
  [character]
  (if (:player character)
    character
    (let [xp (get character :xp 0)
          stats (keys character-driver-stats)]
      (if (pos? xp)
        (recur (raise-stat character (first (shuffle stats))))
        character))))
