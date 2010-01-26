(defproject clog "0.2"
  :dependencies [[org.clojure/clojure "1.1.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.1.0-master-SNAPSHOT"]
                 [compojure "0.3.2" :exclusions [commons-codec
                                                 commons-io
                                                 commons-fileupload
                                                 org.mortbay.jetty/jetty]]
                 [org.xerial/sqlite-jdbc "3.6.16"]]
  :namespaces [clog]
  :main clog)