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
          (let [path (.getAbsolutePath (java-io/file path))]
            (merge {:path path} (load-file (str path "config.clj"))))
          (catch RuntimeException e old-config))))))
