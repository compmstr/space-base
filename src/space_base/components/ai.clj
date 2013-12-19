(ns space-base.components.ai
  (require [lib.entities :as ents])
  (use [lib.queue]
       [lib.vec3]))

(ents/component-type :ai {:fields [:updatefn]})

(defn accel-towards
  [vehicle tgt d-time]
  (let [{:keys [loc vel accel max-speed] :as v} vehicle
        accel-vec (v3-norm (v3- tgt loc))
        vel-mag (min max-speed (+ (v3-mag vel) accel))]
    ;;TODO -- implement accel-towards
    vehicle
    ))
(defn chase
  [vehicle d-time]
  (accel-towards vehicle (:tgt vehicle) d-time))


(defn set-tgt
  [vehicle tgt]
  (assoc vehicle :tgt tgt))
      

(def run-ai (ents/component-fn-runner :ai :updatefn))

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
  (let [self-tasks (atom (queue))
        last-update (atom nil)]
    (defn d-time! []
      (let [cur-time (System/currentTimeMillis)
            prev @last-update]
        (reset! last-update cur-time)
        (- cur-time (or prev cur-time))))
    (defn add-task!
      [task]
      (swap! self-tasks enqueue task))
    (defn peek-task [] (peek @self-tasks))
    (defn pop-task! [] (swap! self-tasks dequeue))
    (fn ai-cmd
      ([cmd & args]
         (case cmd
           :update (ai-cmd)
           :tasks @self-tasks
           :pop-task! (pop-task!)
           :add-task! (add-task! (first args))))
      ([]
         (println "Updating AI after" (d-time!))
         (when-let [cur-task (peek @self-tasks)]
           (if (cur-task :done?)
             (pop-task)
             (cur-task :step)))))))
