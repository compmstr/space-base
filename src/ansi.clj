(ns ansi
  (require [clojure.string :as str]
           [clojure.java.shell :as sh]
           [clojure.java.io :as io]))

(defn esc [] (str (char 27) "["))

(defn do-ansi [& body]
  (print (apply str (esc) body)))
(defn at-xy [r c] (do-ansi r ";" c "H"))
(defn clear [] (do-ansi 2 'J))

(defn save-cursor [] (do-ansi 's))
(defn restore-cursor [] (do-ansi 'u))

(defn hide-cursor [] (do-ansi "?25l"))
(defn show-cursor [] (do-ansi "?25h"))

(def attrs {:reset 0 :normal 22 :bold 1})
(def colors (zipmap [:black :red :green :yellow :blue :magenta :cyan :white] (range)))
(defn attr [& options]
  (letfn [(color [type color]
            (+ (if (= :fg type) 30 40) (colors color)))
          (attr? [k] (contains? (set (keys attrs)) k))
          (color? [k] (contains? (set (keys colors)) k))]
    (loop [acc []
           options options]
      (if (empty? options)
        (do-ansi (apply str (str/join ";" acc)) "m")
        (let [[k arg] options]
          (cond
           (attr? k)
           (recur (conj acc (attrs k))
                  (rest options))
           (or (= k :fg) (= k :bg))
           (recur (conj acc (color k arg))
                  (drop 2 options))))))))

(defmacro with-attrs [options & body]
  `(do (apply attr ~options)
       ~@body
       (attr :reset)))

(defmacro keeping-cursor [& body]
  `(do (save-cursor)
       ~@body
       (restore-cursor)))

(defn reload [] (use 'ansi :reload-all))

(defn execute [cmd]
  (let [p (.. Runtime getRuntime (exec cmd))
        out (. p getInputStream)
        err (. p getErrorStream)
        status (. p waitFor)]
    {:status status :out (slurp out) :err (slurp err)}))

;;Can't switch to character at a time input, stty throws error due to
;;  java's? redirection of stdin
;; Look into JLine
