(ns lib.vec3)

(deftype Vec3 [^double x ^double y ^double z]
  Object
    (equals [self other]
      (and (= (class other) Vec3)
           (= (.x other) (.x self))
           (= (.y other) (.y self))
           (= (.z other) (.z self))))
    (toString [self] (str "(" x "," y "," z ")")))

(defn v3+ [^Vec3 v1 ^Vec3 v2]
	(Vec3. (+ (.x v1) (.x v2))
         (+ (.y v1) (.y v2))
         (+ (.z v1) (.z v2))))

(defn v3- [^Vec3 v1 ^Vec3 v2]
	(Vec3. (- (.x v1) (.x v2))
         (- (.y v1) (.y v2))
         (- (.z v1) (.z v2))))

(defn v3-dot [^Vec3 v1 ^Vec3 v2]
	(+ (* (.x v1) (.x v2))
     (* (.y v1) (.y v2))
     (* (.z v1) (.z v2))))

(defn v3* [^Vec3 v ^double s]
  (Vec3. (* (.x v) s) (* (.y v) s) (* (.z v) s)))

(defn v3-mag [^Vec3 v]
  (Math/sqrt (v3-dot v v)))

(defn v3-norm [^Vec3 v]
  (v3* v (/ 1.0 (v3-mag v))))

(defn v3-man-dist
  [^Vec3 v1 ^Vec3 v2]
  (let [v-sub (v- v1 v2)]
    (+ (Math/abs (.x v-sub))
       (Math/abs (.y v-sub))
       (Math/abs (.z v-sub)))))
(defn v3-sq-dist
  [^Vec3 v1 ^Vec3 v2]
  (v3-dot (v3- v1 v2)))
(defn v3-dist
  (v3-mag (v3- v1 v2)))

;;Test code
(comment 
(defmacro print-test
  [body]
  `(do
     (print '~body ": ")
     (println ~body)))
(def one (Vec3. 1 1 1))
(print-test (v3-norm one))
(print-test (v3-mag (v3-norm one)))
(print-test (v3+ (Vec3. 1 1 1) (Vec3. 1 2 3)))
(print-test (v3- (Vec3. 1 1 1) (Vec3. 1 2 3)))

)
