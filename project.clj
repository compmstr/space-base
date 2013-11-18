(defproject space-base "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :soruce-path "src"
  :main space-base.core
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;Add src/natives to the path where java looks up native libs
  :jvm-opts [~(str "-Djava.library.path=src/natives:"
                   (System/getProperty "java.library.path"))]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.lwjgl.lwjgl/lwjgl "2.8.5"]])
