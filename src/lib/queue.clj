(ns lib.queue)

(defmethod print-method clojure.lang.PersistentQueue
  [q w]
  (print-method '<- w)
  (print-method (seq q) w)
  (print-method '-< w))

(defn enqueue
  [q & elts]
  (if elts (apply conj q elts) q))
(defn dequeue
  [q]
  (pop q))

(defn queue
  [& elts]
  (apply enqueue clojure.lang.PersistentQueue/EMPTY elts))
