(ns space-base.ui.window
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [com.badlogic.gdx.graphics GL10]
           [com.badlogic.gdx.scenes.scene2d Stage]
           [com.badlogic.gdx ApplicationListener Gdx Screen]))

(set! *warn-on-reflection* true)

(def ^:const WIDTH (int 800))
(def ^:const HEIGHT (int 600))

(defmacro on-render-thread
  [& body]
  `(.. Gdx app
       (postRunnable (fn []
                       ~@body))))

(def stage (atom nil))

;;The screen proxy and setScreen give
;;  delta on render, but probably not needed with
;;  how I'm going to implement physics/updates
(defn game-screen
  []
  (proxy [Screen] []
    (show []
      (.. Gdx gl (glClearColor 0.2 0.5 1.0 0.0))
      (.. Gdx gl10 (glViewport (int 0) (int 0) WIDTH HEIGHT))
      (reset! stage (Stage. WIDTH HEIGHT)))
    (render [delta]
      (.. Gdx gl (glClear GL10/GL_COLOR_BUFFER_BIT))
      (doto ^Stage @stage
            (.act)
            (.draw)))
    (dispose [])
    (hide [])
    (pause [])
    (resize [w h])
    (resume [])))

(deftype SpaceBase []
    ApplicationListener
  (create [this]
    (.. Gdx gl (glClearColor 0.2 0.5 1.0 0.0))
    (.. Gdx gl10 (glViewport (int 0) (int 0) WIDTH HEIGHT))
    (reset! stage (Stage. WIDTH HEIGHT)))
  (dispose [this])
  (pause [this])
  (render [this]
    (.. Gdx gl (glClear GL10/GL_COLOR_BUFFER_BIT))
    (doto ^Stage @stage
      (.act)
      (.draw)))
  (resize [this w h])
  (resume [this]))

(defn start []
  (LwjglApplication.
   (SpaceBase.)
   "Space Base"
   WIDTH HEIGHT
   false))
