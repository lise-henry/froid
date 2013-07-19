(ns froid.gui
  (:use [froid.ascii]
        [froid.core]
        [froid.circuit]))

(def ^:private main-frame (javax.swing.JFrame. "FROID"))

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


(defn update-rankings!
  [model drivers]
  """(AbstractTableModel, Drivers) -> AbstractTableModel
    Update the table model with appropriate data and headers.
    Returns the model."""
    (let [data (map #(vector (inc %1)
                             (:name %2)
                             (:team %2)
                             (time-diff %2
                                        (:time (first drivers))))
                    (range)
                    drivers)
          headers ["Pos" "Name" "Team" "Time"]]
      (.setDataVector model
                      (to-array-2d data)
                      (into-array headers))
      model))

;; (defn edit-driver
;;   [driver]
;;   """Driver -> Driver
;;      Display the driver's state in a new window, with
;;      possibility to raise them"""
     

(defn display-character-panel!
  [component driver]
  """(Character, JPanel) -> ()
     Displays the driver stats in appropriate component"""
     (let [editable (and 
                     (:player driver)
                     (> (:xp driver)
                        0))
           panel (javax.swing.JPanel.)
           layout (javax.swing.BoxLayout. panel javax.swing.BoxLayout/Y_AXIS)]
       (.setLayout panel layout)
       (.add component panel)
       (doseq [[k v] driver]
         (let [label (javax.swing.JLabel. (str k ": " v))]
         (.add panel
               (if editable
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
                          (spit "/tmp/miaou.clj"
                                (assoc-in 
                                 (read-string (slurp "/tmp/miaou.clj"))
                                 [:drivers (:name character)]
                                 character))
                          (.remove component panel)
                          (display-character-panel! component
                                                    character)
                          (.pack component)))))
                   (doto line-panel
                     (.setLayout layout)
                     (.add label)
                     (.add button)))
                 label))))))

(defn display-character
  [driver]
  """Driver -> ()
     Display the driver's stats in a new window"""
     (let [frame (javax.swing.JFrame. (:name driver))]
       (doto frame
         (display-character-panel! driver)
         (.pack)
         (.setVisible true))))
 
(defn create-gui
  [characters]
  """() -> (JPanel, (label, drivers) -> ()))
     Returns a vector containing: 
     - a panel to add in a JFrame
     - an update function running a lap and updating given panel"""
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
                       (update-rankings! model drivers))]
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
                                    (((read-string 
                                       (slurp "/tmp/miaou.clj")) 
                                      :drivers)
                                     name)))))))
       [panel, update-fn]))

(defn gui-main
  []
  """() -> ()
     Main function launching the GUI"""
     (let [;;drivers (map froid.core/character->Driver (vals ((froid.init/init-all 5 3) :drivers)))
           characters (vals ((read-string (slurp "/tmp/miaou.clj")) :drivers))
           drivers (map froid.core/character->Driver characters)
           circuit (froid.circuit/random-circuit)
           drivers (sort-by :lap-time 
                            (map #(qual-time % circuit) 
                                 drivers))
           [panel update!] (froid.gui/create-gui characters)]
    (doto main-frame
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
            (Thread/sleep 1000)
            (recur (froid.circuit/time-step drivers circuit) (inc l)))))))
       
