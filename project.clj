(defproject space-base "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :soruce-path "src"
  :main space-base.core
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/snapshots/"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;Add src/natives to the path where java looks up native libs
  ;;:jvm-opts [~(str "-Djava.library.path=src/natives:"
  ;;                 (System/getProperty "java.library.path"))]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.badlogicgames.gdx/gdx "1.0-SNAPSHOT"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.0-SNAPSHOT"]
                 [com.badlogicgames.gdx/gdx-platform "1.0-SNAPSHOT"
                  :classifier "natives-desktop"]])
