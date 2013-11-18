(ns space-base.ui.window
  (:import [org.lwjgl.opengl Display DisplayMode]))

(defn start []
  (Display/setDisplayMode (DisplayMode. 800 600))
  (Display/create)
  (loop []
    (if (Display/isCloseRequested)
      (println "Exiting")
      (do
        (Display/update)
        (recur))))
  (Display/destroy))
