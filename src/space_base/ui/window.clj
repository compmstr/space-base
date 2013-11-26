(ns space-base.ui.window
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [com.badlogic.gdx.graphics GL10]
           [com.badlogic.gdx.scenes.scene2d Stage]
           [com.badlogic.gdx ApplicationListener Gdx]))

(set! *warn-on-reflection* true)

(def ^:const WIDTH (int 800))
(def ^:const HEIGHT (int 600))

(defmacro on-render-thread
  [& body]
  `(.. Gdx app
       (postRunnable (fn []
                       ~@body))))

(def stage (atom nil))

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
