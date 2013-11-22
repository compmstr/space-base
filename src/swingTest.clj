(ns swingTest
  (import
   [java.awt Dimension Color]
   [javax.swing JFrame JPanel Timer]
   [javax.swing.event MouseInputAdapter]
   [java.awt.event ActionListener ActionEvent MouseEvent]))

(defn add-vec
  [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])
(defn sub-vec
  [[x1 y1] [x2 y2]]
  [(- x1 x2) (- y1 y2)])
(defn mult-vec
  [[x y] s]
  [(* x s) (* y s)])

(defn dot-prod
  [[x1 y1] [x2 y2]]
  (+ (* x1 x2) (* y1 y2)))

(defn vec-mag
  [v]
  (Math/sqrt (dot-prod v v)))
(defn vec-norm
  [v]
  (let [mag (vec-mag v)]
    (if (zero? mag)
      [0 0]
      (mult-vec v (/ 1.0 (vec-mag v))))))

(defn guy [loc vel]
  {:loc loc :vel vel})
(def guys (atom [(guy [5 5] [0 0])
                 (guy [100 100] [0 0])
                 (guy [200 5] [0 0])
                 (guy [200 20] [0 0])]))
(def target (atom [400 300]))

(def ^:const guy-diam 20)
(def ^:const guy-radius 10)

(defn draw-guys
  [g]
  (doseq [{[x y] :loc} @guys]
    (.fillArc g (- x guy-radius) (- y guy-radius) guy-diam guy-diam 0 360)))

(defn draw-target
  [g]
  (let [[tx ty] @target]
    (doto g
      (.drawArc (- tx 5) (- ty 5) 10 10 0 360)
      (.drawLine tx (- ty 10) tx (+ ty 10))
      (.drawLine (- tx 10) ty (+ tx 10) ty))))

(defn anim-panel
  []
  (let [panel (proxy [JPanel] []
                (paintComponent [g]
                  (doto g
                    (.clearRect 0 0 800 600)
                    (draw-guys)
                    (draw-target))))]
    (doto panel
      (.setPreferredSize (Dimension. 800 600))
      (.setOpaque true))
    panel))

(def rot-speed (* 2 Math/PI))
(def ^:const max-speed 100)
(def ^:const accel 200)

(defn seek
  [tgt d-time {loc :loc vel :vel :as cur-guy}]
  (let [tgt-vec (vec-norm (sub-vec tgt loc))
        accel-vec (mult-vec tgt-vec (* accel d-time))
        acceled-vel (add-vec accel-vec vel)
        new-speed (min max-speed (vec-mag acceled-vel))
        new-vel (mult-vec (vec-norm acceled-vel) new-speed)]
    (guy loc new-vel)))
(let [last-update (atom nil)]
  (defn update-guy
    [d-time {[x y] :loc vel :vel}]
    (let [[vX vY] vel]
      (seek @target d-time 
            (guy [(+ x (* vX d-time))
                  (+ y (* vY d-time))]
                 vel))))
  (defn update-guys
    []
    (if-let [prev-time @last-update]
      (let [cur-time (System/currentTimeMillis)
            delta (- cur-time prev-time)
            d-time (/ delta 1000.0)]
        (swap! guys #(map (partial update-guy d-time) %))
        (reset! last-update cur-time))
      (reset! last-update (System/currentTimeMillis)))))

(def stop-anim (atom nil))
(defn animate
  [component]
  (let [fps 30
        delay (/ (float 1000) fps)
        timer (Timer. delay nil)
        listener (proxy [ActionListener] []
                   (actionPerformed [e]
                     (update-guys)
                     (.repaint component)
                     (when @stop-anim
                       (.stop timer))))]
    (doto timer
      (.addActionListener listener)
      (.start))))
                       
(defn target-updater
  []
  (proxy [MouseInputAdapter] []
    (mousePressed [e]
      (when (= MouseEvent/BUTTON1 (.getButton e))
        (reset! target [(.getX e) (.getY e)])))))

(defn runTest
  []
  (let [panel (anim-panel)
        frame (JFrame.)]
    (.addMouseListener panel (target-updater))
    (doto frame
      (.setContentPane panel)
      (.pack)
      (.setLocationRelativeTo nil)
      (.setVisible true))
    (animate panel)))

