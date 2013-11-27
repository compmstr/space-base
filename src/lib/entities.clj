(ns lib.entities
  (require [clojure.set :as set]))

;;A component type is a map of type names to data
;; :fields is required, and is a vector of :names or field definitions
;;   field definitions are maps with required :name, and optional :default values
;; :deps is optional, and is a vector of other component types that are required for this entity
;;  {:fields [(:name|{:name ... <:default ...>})]
;;    <:deps [:other-component ...]>}
(def component-types (atom {}))
(defn valid-component? [[k v]]
  (letfn [(valid-field [f]
            (or (keyword? f)
                (contains? f :name)))
          (valid-fields? [c]
            (and (contains? c :fields)
                 (every? valid-field (:fields c))))
          (valid-deps? [c]
            (or (not (contains? c :deps))
                (every? keyword? (:deps c))))]
    (and (keyword? k)
         (valid-fields? v)
         (valid-deps? v))))
(defn component-type
  ([c-type c-data]
     (component-type [c-type c-data]))
  ([c]
     (if (valid-component? c)
       (swap! component-types merge c)
       (println "Error: Component:" c "is invalid"))))
;;Entity Types are a map of type names to a set of needed components as maps (can be empty maps
;;  if all fields have defaults)
(def entity-types (atom {}))

(defn component-needed-fields [comp-type]
  (loop [fields (:fields (@component-types comp-type))
         needed #{}
         defaulted #{}]
    (if (empty? fields)
      {:needed needed
       :defaulted defaulted}
      (let [[f & fs] fields]
        (if (keyword? f)
          (recur fs (conj needed f) defaulted)
          (recur fs needed (conj defaulted (:name f))))))))
(defn component-has-needed-fields?
  ([[name comp]]
     (component-has-needed-fields? name comp))
  ([name comp]
     (let [needed (:needed (component-needed-fields name))
           not-provided (set/difference needed (keys comp))]
       (if (empty? not-provided)
         true
         (do
           (println "Component of type:" name "missing fields:" not-provided)
           nil)))))
(defn entity-dependencies-met? [ent]
  (let [components (set (keys ent))
        deps (set (mapcat :deps (map @component-types components)))
        missing (set/difference deps components)]
    (if (empty? missing)
      true
      (do (println "Missing dependencies in entity(" ent "):" missing)
          nil))))
(defn valid-entity? [ent] 
  (and (entity-dependencies-met? ent)
       (every? component-has-needed-fields? ent)))

(defn create-entity-type [name ent]
  (when (valid-entity? ent)
    (swap! entity-types assoc name ent)))

;;Components is {:component-type -> {id -> component}}
;;  Components map is atom, each component type map is an agent
;;  Each component ends up with it's own id
(def components (atom {}))

(defn component-type>defaults
  [type]
  (into {}
        (map (juxt :name :default)
             (remove keyword?
                     (get-in @component-types [type :fields])))))
(defn component-type>instance
  [id type vals]
  (merge (component-type>defaults type) vals {:id id}))

(defn create-component
  [id [type vals]]
  (swap! components
         (fn [comps]
           (let [new-comp (component-type>instance id type vals)]
             (if-let [type-agent (type comps)]
               (do
                 (send type-agent assoc id new-comp)
                 comps)
               (assoc comps type
                      (agent {id new-comp})))))))

(defn new-id [] (. clojure.lang.RT (nextID)))

(defn create-entity
  [ent-type & extra-components]
  (let [extra-ent (into {} (map vec (partition 2 extra-components)))
        entity (merge-with merge (@entity-types ent-type)
                      extra-ent)]
    (when (valid-entity? entity)
      (let [ent-id (new-id)]
        (doseq [comp entity]
          (create-component ent-id comp))
        ent-id))))

(defn component-fn-runner
  "Creates a function that runs all of the <fn-key> functions within the
  <component-type> components"
  [component-type fn-key]
  (fn []
    (doseq [component (vals @(component-type @components))]
      ((component fn-key) (:id component)))))

(defn remove-entity
  [id]
  (doseq [comp-type (vals @components)]
    (send comp-type dissoc id)))

(defn entity-by-id
  [id]
  (let [comps @components
        entity (->>
                (keys comps)
                (map (fn [k] [k (get @(comps k) id)]))
                (filter second)
                (mapcat identity)
                (apply hash-map))]
    (if (empty? entity)
      nil
      entity)))

(defn get-comp
  [comp id]
  (get @(@components comp) id))

;; Test code

(comment

(def test-components
  [[:walking {:fields [{:name :speed :default 5}]}]
   [:position {:fields [:loc :facing]}]
   [:player {:fields [:inputmap]}]
   [:dynamic {:fields [:vecocity :mass] :deps [:position]}]])

;;Example components with systems
;;Damagable system
(component-type :damagable {:fields [:hp :max-hp {:name :armor :default 5}]})
(defn armor-scale
  "Aim for about 65% damage reduction at 100 armor"
  [armor]
  (- 1.0 (/ (max 0 (Math/log armor)) 7)))
;;TODO -- this appears broken
(defn damage [id amt]
  (dosync
   (let [armor (get-in @components [:damagable id :armor])]
   (alter components
          update-in [:damagable id :hp] #(- (* % (armor-scale armor)))))))
(defn heal [id amt]
  (dosync
   (let [max-hp (get-in @components [:damagable id :max-hp])]
     (alter components
            update-in [:damagable id :hp] #(min (+ % amt) max-hp)))))

;;Drawable system
(component-type :drawable {:fields [:drawfn] :deps [:position]})
(def draw-entities (component-fn-runner :drawable :drawfn))

;;AI system
(component-type :ai {:fields [:updatefn]})
(def run-ai (component-fn-runner :ai :updatefn))

;;END --- Example components with systems


(def get-position (partial get-comp :position))

(require 'ansi)
(defn ent-ansi-drawer
  [sym]
  (fn [id]
    (apply ansi/at-xy (reverse (get-in @components [:position id :loc])))
    (prn sym)))

(doall (map component-type test-components))

(defn drawer
  [name id]
  (let [[x y] (:loc (get-position id))]
    (println name (str "(" id ")") "@" x y)))

(create-entity-type :player
                    {:walking {}
                     :player {:inputmap {:left #(println "player with id:" % "go left")}}
                     :damagable {:hp 10 :max-hp 10}
                     :drawable {:drawfn (partial drawer "player")}
                     :position {:loc [4 5] :facing 0.0}})
(create-entity-type :mob1
                    {:walking {}
                     :ai {:updatefn (fn [id] (println "Do-AI for id:" id "at:" (:loc (get-position id))))}
                     :damagable {:hp 5 :max-hp 5}
                     :drawable {:drawfn (partial drawer "mob1")}
                     :position {:loc [10 10] :facing 0.0}})

(def player-id (create-entity :player))
(def mob1-id (create-entity :mob1))
;;You can override portions of components (ex: only override :loc in :position)
;;  this is due to the (merge-with merge in create-entity)
(def mob2-id (create-entity :mob1 :position {:loc [15 10]} :ai {:updatefn (fn [id] (println "Do-AI2 for id:" id "at" (:loc (get-position id))))}))

(defn reload-ents [] (use 'lib.entities :reload-all)
  (require '[clojure.set :as set]))

) ;;comment end
