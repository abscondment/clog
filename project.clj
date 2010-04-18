(defproject clog "0.3"
  :repositories {"scala-tools" "http://scala-tools.org/repo-releases/"}
  :dependencies [[org.clojure/clojure "1.2.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.2.0-SNAPSHOT"]
                 [clj-yaml "0.2.0-SNAPSHOT"]
                 [compojure "0.3.2" :exclusions [commons-codec
                                                 commons-io
                                                 commons-fileupload
                                                 org.mortbay.jetty/jetty]]
                 [org.markdownj/markdownj "0.3.0-1.0.2b4"]]
  :dev-dependencies [[leiningen/lein-swank "1.1.0"]
                     [leiningen-run "0.3"]]
  :namespaces [clog]
  :main clog.core)