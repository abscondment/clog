(ns clog.config
  (:require [clojure.java.io :as java-io]))

(comment
 (def *config* {:title "Quod erat faciendum"
                :subtitle "A technical blog by Brendan Ribera"
                :author "Brendan Ribera"
                :domain "threebrothers.org"
                :path "/home/brendan/code/threebrothers.org"
                :static-paths ["/brendan/"
                               "/brendan/about/"
                               "/brendan/software/"]
                }))

(def ^{:private false} *config* nil)

(defn do-read-config
  ([] (do-read-config (System/getenv "PWD")))
  ([path]
     (alter-var-root
      #'*config*
      (fn [_]
        (load-file
         (.getAbsolutePath (java-io/file path "config.clj")))))))
