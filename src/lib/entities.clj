(ns lib.entities)

(def test-components
  [[:damagable {:fields [:hp {:name :armor :default 5}]}]
   [:position {:fields [:loc :facing]}]
   [:dynamic {:fields [:vecocity :mass] :deps [:position]}]])

;;A component type is a map of type names to data
;; :fields is required, and is a vector of :names or field definitions
;;   field definitions are maps with required :name, and optional :default values
;; :deps is optional, and is a vector of other component types that are required for this entity
;;  {:fields [(:name|{:name ... <:default ...>})]
;;    <:deps [:other-component ...]>}
(def component-types (ref {}))
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
  [c]
  (if (valid-component? c)
    (dosync
     (alter component-types
            merge c))
    (println "Error: Component:" c "is invalid")))
;;Entity Types are a map of type names to a set of needed components
;;  (either the component type keyword, or a pre-made component)
(def entity-types (ref {}))

;;Components is {:component-type -> {id -> component}}
;;  Each component ends up with it's own id
(def components (ref {}))

(defn new-id [] (. clojure.lang.RT (nextID)))
