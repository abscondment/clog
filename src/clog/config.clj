(ns clog.config
  (:require [clojure.java.io :as java-io]))

(def ^{:dynamic true :private false} *config* nil)

(defn do-read-config
  ([] (do-read-config (System/getenv "PWD")))
  ([path]
     (alter-var-root
      #'*config*
      (fn [old-config]
        (try
          (load-file
           (.getAbsolutePath (java-io/file path "config.clj")))
          (catch RuntimeException e old-config))))))
