(ns space-base.components.drawable
  (require '[lib.entities :as ents]))

(ents/component-type :drawable {:fields [:drawfn] :deps [:position]})

(def draw-entities (component-fn-runner :drawable :drawfn))
