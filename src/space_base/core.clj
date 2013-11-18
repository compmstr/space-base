(ns space-base.core
  (require [space-base.ui.window :as window]
           ansi))

(defn -main [& args]
  (window/start))
