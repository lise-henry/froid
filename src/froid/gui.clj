(ns froid.gui
  (:use [froid.ascii]
        [froid.core]
        [froid.circuit]
        [froid.init]))

(defn- prompt-name
  [title, text]
  """(string, string) -> string
     Shows a JDialog with title and text, and return the string
     entered by the user."""
     (javax.swing.JOptionPane/showInputDialog nil
                                              text
                                              title
                                              javax.swing.JOptionPane/PLAIN_MESSAGE
                                              nil,
                                              nil,
                                              ""))

(defn prompt-names
  [n]
  """(int) -> ?
     Shows a JDialog asking for player's team name and drivers names.
     n is the number of drivers in the team."""
     (let [team (prompt-name "Team" "Enter team name")]
       (if (nil? team)
         nil
         (loop [v []
                i 0]
           (if (= i n)
             {team v}
             (let [name (prompt-name (str "Driver " (inc i))
                                     "Enter driver name")]
               (if (nil? name)
                 nil
                 (recur (conj v name) (inc i)))))))))

(defn init-all
  [n-teams n-drivers]
  """(int int) -> data
     Returns a list of teams and a list of drivers.
     There is n-teams (plus player's one) and n-drivers by team."""
  (let [teams (teams-generator n-teams n-drivers)
        player-team (froid.gui/prompt-names n-drivers)
        player-name (first (keys player-team))
        teams (into teams player-team)]
    (if (nil? player-team)
      nil
      {:teams teams
       :drivers (drivers-generator-from-teams teams
                                              player-name)})))


(defn rankings!
  "(AbstractTableModel, Drivers, bool) -> AbstractTableModel
    Update the table model with appropriate data and headers.
    Returns the model."
  ([model drivers]
     (rankings! model drivers :time))
  ([model drivers stat]
     (let [data (map #(vector (inc %1)
                              (:name %2)
                              (:team %2)
                              (if (= stat :points)
                                (stat %2)
                                (time-diff (stat %2)
                                           (stat (first drivers)))))
                     (range)
                     drivers)
           headers (if (= stat :points) 
                          ["Pos" "Name" "Team" "Points"]
                          ["Pos" "Name" "Team" "Time"])]
       (.setDataVector model
                       (to-array-2d data)
                       (into-array headers))
       model)))

;; (defn edit-driver
;;   [driver]
;;   """Driver -> Driver
;;      Display the driver's state in a new window, with
;;      possibility to raise them"""
     

(defn- display-character-panel!
  "(Character, JPanel) -> ()
   Displays the driver stats in appropriate component"
  [component driver]
  (let [editable (and 
                  (:player driver)
                  (> (:xp driver)
                     0))
        panel (javax.swing.JPanel.)
        layout (javax.swing.BoxLayout. panel javax.swing.BoxLayout/Y_AXIS)]
    (.removeAll component)
    (.setLayout panel layout)
    (.add component panel)
    (doseq [[k v] driver]
      (let [label (javax.swing.JLabel. (str (name k) ": " v))]
        (.add panel
              (if (and editable
                       (k character-driver-stats))
                (let [line-panel (javax.swing.JPanel. )
                      layout (javax.swing.BoxLayout.
                              line-panel javax.swing.BoxLayout/X_AXIS)
                      button (javax.swing.JButton. "+")]
                  (.addActionListener 
                   button
                   (proxy [java.awt.event.ActionListener] []
                     (actionPerformed [e] 
                       (let [character (raise-stat driver
                                                   k)]
                         (set-data!
                          (assoc-in 
                           (get-data)
                           [:drivers (:name character)]
                           character))
                         (display-character-panel! component
                                                   character)
                         (.pack component)))))
                  (doto line-panel
                    (.setLayout layout)
                    (.add label)
                    (.add button)))
                label))))))

(defn display-character
  "Character -> ()
   Display the driver's stats in a new window"
  [driver]
  (let [frame (javax.swing.JFrame. (:name driver))]
    (doto frame
      (display-character-panel! driver)
      (.pack)
      (.setVisible true))))

(defn display-player-characters
  "() -> ()
   Display a frame with the player's character and proposes to modify them"
  []
  (let [characters (filter :player (vals (:drivers (get-data))))
        team (:team (first characters))
        frame (javax.swing.JFrame. team)
        outer-panel (javax.swing.JPanel.)
        inner-panel (javax.swing.JPanel.)]
    (doseq [c characters]
      (let [button (javax.swing.JButton. (:name c))]
        (.addActionListener button
                            (proxy [java.awt.event.ActionListener] []
                              (actionPerformed [e]
                                (display-character-panel! inner-panel c)
                                (.pack frame))))
        (.add outer-panel button java.awt.BorderLayout/PAGE_START)))
    (.add outer-panel inner-panel java.awt.BorderLayout/PAGE_END)
    (doto frame
      (.add outer-panel)
      (.pack)
      (.setVisible true))))
  

(defn create-gui
  "() -> (JPanel, (label, drivers) -> ()))
     Returns a vector containing: 
     - a panel to add in a JFrame
     - an update function running a lap and updating given panel"
  []
  (let [panel (javax.swing.JPanel.)
        label (javax.swing.JLabel.)
        model (javax.swing.table.DefaultTableModel.)
        table (javax.swing.JTable. model)
        scroll-pane (javax.swing.JScrollPane. table)
        panel (doto panel
                (.setLayout (javax.swing.BoxLayout.
                             panel
                             javax.swing.BoxLayout/Y_AXIS))
                (.add label)
                (.add scroll-pane))
        update-fn (fn [text drivers]
                    (.setText label text)
                    (let [stat (condp = text
                                 "Qualifications" :lap-time
                                 "Championship" :points
                                 :time)]
                      (rankings! model drivers stat)))]
    (.addMouseListener table
                       (proxy [java.awt.event.MouseAdapter] []
                         (mouseClicked [e]
                           (let [point (.getPoint e)
                                 row (.rowAtPoint table point)
                                 name (str (.getValueAt 
                                            table
                                            row
                                            1))]
                             (;; todo: baaaaaaaaad
                              (display-character
                               (((get-data) :drivers) name)))))))
    [panel, update-fn]))

(declare run-race)

(defn champ-rankings
  "([panel, updatefn], drivers) -> ()
   Calculate championship results and displays them in the frame"
  [frame drivers]
  (let [[panel update!] (create-gui)
        data (post-race drivers (get-data))
        button (javax.swing.JButton. "New race")]
    (set-data! data)
    (update! "Championship" (reverse (sort-by :points
                                              (vals (:drivers data)))))
    (.addActionListener button
                        (proxy [java.awt.event.ActionListener] []
                          (actionPerformed [e]
                            (run-race frame))))
    (.removeAll (.getContentPane frame))
    (.add panel button)
    (.add (.getContentPane frame) panel)
    (.pack frame)))


(defn run-race
  "JFrame -> ()
   Runs a race and displays it in the frame"
  [frame]
  (let [characters (vals ((get-data) :drivers))
        drivers (map froid.core/character->Driver characters)
        circuit (froid.circuit/random-circuit)
        drivers (vec (sort-by :lap-time 
                         (map #(qual-time % circuit) 
                              drivers)))
        [panel update!] (create-gui)
        toto (fn toto [l drivers]
               (if (< l 49)
                 (let [timer (javax.swing.Timer. 1000
                                                 (proxy [java.awt.event.ActionListener] []
                                                   (actionPerformed [e]
                                                     (toto (inc l) (froid.circuit/time-step drivers circuit)))))]
                   (update! (str "Lap " (inc l)) drivers)
                   (.setRepeats timer false)
                   (.start timer))
                 (let [button (javax.swing.JButton. "Championship results")]
                   (update! "Race results" drivers)
                   (.addActionListener button
                                       (proxy [java.awt.event.ActionListener] []
                                         (actionPerformed [e]
                                           (.remove panel button)
                                           (champ-rankings frame drivers))))
                   (.add panel button))))]
    (.removeAll (.getContentPane frame))
    (.add (.getContentPane frame) panel)
    (.pack frame)
    (update! "Qualifications" drivers)
    (let [button (javax.swing.JButton. "Start race!")]
      (.addActionListener button
                          (proxy [java.awt.event.ActionListener] []
                            (actionPerformed [e]
                              (.remove panel button)
                              (toto 0 (froid.circuit/time-step drivers circuit)))))
      (.add panel button))))


(defn gui-main
  []
  """() -> ()
     Main function launching the GUI
  """
;;drivers (map froid.core/character->Driver (vals ((froid.init/init-all 5 3) :drivers)))
  (let [frame (javax.swing.JFrame. "FROID")
        button-race (javax.swing.JButton. "Run race")
        button-new (javax.swing.JButton. "New")
        panel (javax.swing.JPanel.)]
    (.addActionListener button-new
                        (proxy [java.awt.event.ActionListener] []
                          (actionPerformed [e]
                            (set-data! (init-all 7 3)))))
    (.addActionListener button-race
                        (proxy [java.awt.event.ActionListener] []
                          (actionPerformed [e]
                            (run-race frame))))
    (.add (.getContentPane frame) panel)
    (doto panel
      (.add button-new)
      (.add button-race))
    (doto frame
     (.setDefaultCloseOperation javax.swing.JFrame/EXIT_ON_CLOSE)
     (.pack)
     (.setVisible true))))






