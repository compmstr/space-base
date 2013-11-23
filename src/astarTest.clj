(ns astarTest
	(require ansi
           [clojure.set :as set]
           [lib.vec3 :as vec3])
  (import lib.vec3.Vec3))

(def ^:const map-size 8)
(def tile-cost
  {0 1
   1 1.5
   2 3
   9 8})
(def tile-glyph
  {0 \.
   1 \*
   2 \~
   9 \^})
(def test-map [0 0 0 0 0 0 0 0
               0 0 0 0 1 1 9 0
               0 0 1 1 2 2 9 0
               0 0 1 9 2 2 9 0
               0 1 9 9 9 9 9 0
               0 0 1 1 1 1 1 0
               0 0 0 0 0 0 0 0
               0 0 0 0 0 0 0 0])
(defn draw-map
  [m]
  (doseq [l (partition map-size
                       (map tile-glyph m))]
    (println (apply str (interpose " " l)))))
(defn map-tile
  [m loc]
  (nth m (+ (.x loc) (* (.y loc) map-size))))

(defn neighbors
  [m {loc :loc}]
  (let [locs (for [x (range (dec (.x loc)) (+ 2 (.x loc)))
                   y (range (dec (.y loc)) (+ 2 (.y loc)))
                   :when (and (>= x 0) (>= y 0)
                              (or (not= (.x loc) x) (not= (.y loc) y))
                              (< x map-size) (< y map-size))]
               (Vec3. x y 0))]
    (map (fn [loc]
           (let [tile (map-tile m loc)
                 cost (or (tile-cost tile) 1)]
             {:loc loc :tile tile :cost cost}))
         locs)))

;;nav-node keys: -- :prev :loc :cost
(def est-cost vec3/v3-man-dist)
;;(def est-cost vec3/v3-dist)
(defn total-cost
  [tgt n]
  (+ (est-cost tgt (:loc n))
     (:cost n)))
(defn nav-node
  [prev m tgt loc]
  (if prev
    (let [new-loc (:loc loc)]
      {:loc new-loc
       :cost (+ (:cost loc)
                (:cost prev))
       :prev prev})
    {:loc (:loc loc)
     :cost 0
     :prev nil}))

(defn build-path
  [n]
  (loop [cur n
         acc '()]
    (if (nil? cur)
      acc
      (recur (:prev cur)
             (conj acc (:loc cur))))))

(defn navigate-a*
  [m start tgt]
  (loop [to-check [(nav-node nil m tgt {:loc start})]
         visited []]
    (comment
    (println "================")
    (println (:loc (first to-check))
             (:cost (first to-check))
             (total-cost tgt (first to-check)))
    ;(println 'to-check)
    ;(clojure.pprint/pprint (take 3 to-check))
    ;(println 'visited)
    ;(clojure.pprint/pprint visited)
    (println "================")
    ;(Thread/sleep 1000)
    )
    (let [cur (first to-check)]
      (if (= (:loc cur) tgt)
        (build-path cur)
        (let [created-locs (set/union
                            (set (map :loc visited))
                            (set (map :loc to-check)))
              new-ns (remove #(contains? created-locs (:loc %))
                             (map (partial nav-node cur m tgt) (neighbors m cur)))]
          (let [new-to-check (sort-by (partial total-cost tgt) (concat (rest to-check) new-ns))]
            (if (= 0 (count new-to-check))
              nil
              (recur new-to-check
                     (conj visited cur)))))))))
          
