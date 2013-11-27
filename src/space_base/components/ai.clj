(ns space-base.components.ai
  (require '[lib.entities :as ents])
  (use '[lib.queue]))

(ents/component-type :ai {:fields [:updatefn]})

(def run-ai (component-fn-runner :ai :updatefn))

;;Tasks are functions that take two main keys
;;  :step, to do a step of the task
;;  and :done? to check if a task is done
;;TODO: how to xfer data to next subtask
;;  ex: navigate-to needs to pass path to go-to
;;When creating a task, it can have a data atom to
;;  use to transfer data between steps, each task
;;  type will use this data differently
;;  The data atom will be passed to it as a parameter
;;    and it will fill it or not based on the task
;;Tasks will need to have some sort of ID to make sure that
;;  multiple entities don't have the same task
;;When creating an AI -- need to pass it a map of task type -> priority
;;  that it can return based on input to update it

(defn gen-ai
  []
  (let [self-tasks (atom (queue))]
    (defn add-task
      [task]
      (swap! self-tasks enqueue task))
    (defn peek-task [] (peek @self-tasks))
    (defn pop-task [] (swap! self-tasks dequeue))
    (fn []
      (println "Updating AI")
      (let [cur-task (peek @self-tasks)]
        (if (cur-task :done?)
          (pop-task)
          (cur-task :step))))))
