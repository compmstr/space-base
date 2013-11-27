(ns space-base.components.position
  (require '[lib.entities :as ents]))

(ents/component-type :position {:fields [:loc :facing]})

(def get-position (partial ents/get-comp :position))
