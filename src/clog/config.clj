(ns clog.config
  (:require [clojure.java.io :as java-io]))

(def ^{:dynamic true :private false} *config* nil)

(defn- massage-root-path
  "Remove all trailing and leading /es."
  [path]
  (let [strip-slashes (fn [s] (->> s (drop-while #(= \/ %)) (reverse)))]
    (apply str (-> (seq path) (strip-slashes) (strip-slashes)))))

(defn do-read-config
  "Read config.clj, and swap it in to *config*."
  ([] (do-read-config (System/getenv "PWD")))
  ([path]
     (alter-var-root
      #'*config*
      (fn [old-config]
        (try
          (let [path (.getAbsolutePath (java-io/file path))
                new-config (load-file (str path "/config.clj"))]
            (merge new-config
                   {:path path
                    :root-path (massage-root-path (:root-path new-config))}))
          (catch RuntimeException e old-config))))))
