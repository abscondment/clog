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
  ([] (do-read-config (or (System/getProperty "user.dir") (System/getenv "PWD"))))
  ([path]
     (alter-var-root
      #'*config*
      (fn [old-config]
        (try
          (let [config-file (java-io/file path "config.clj")
                new-config (load-file (str config-file))]
            (merge new-config
                   {:path (.getCanonicalPath (.getParentFile config-file))
                    :root-path (massage-root-path (:root-path new-config))}))
          (catch RuntimeException e old-config))))))
