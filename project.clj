(defproject clog "0.5"
  :repositories {"scala-tools" "http://scala-tools.org/repo-releases/"}
  :dependencies [[org.clojure/clojure "1.2.0-RC3"]
                 [org.clojure/clojure-contrib "1.2.0-RC3"]
                 [enlive "1.0.0-SNAPSHOT"]
                 [clj-yaml "0.2.0-SNAPSHOT"]
                 [org.markdownj/markdownj "0.3.0-1.0.2b4"]]
  :dev-dependencies [[swank-clojure "1.2.1"]
                     [leiningen-run "0.3"]]
  :main clog.core)