(ns clog.generate
  (:require [clojure.string])
  (:use
   [clojure.java.io :only [file]]
   [clog config helpers]))

(defn- pretty-url [title]
  (-> title
      (clojure.string/lower-case)
      (clojure.string/replace (java.util.regex.Pattern/compile
                               (str "[" java.io.File/separator "]+")) " ")
      (clojure.string/replace #"[^a-zA-Z0-9]+" " ")
      (clojure.string/replace #"[ \n\t]+" "-")
      (clojure.string/replace #"[\\-]+", "-")
      (clojure.string/replace #"[\\-]+$", "")))

(defn- do-generate-post-dir [dir title]
  (do
    (.mkdirs dir)
    (spit (file dir "post.markdown") "## Edit me\n")
    (spit (file dir "post.yaml")
          (str "---
title: \"" title "\"
created_at: " (date-adjust-timezone
               (format-date
                (java.util.Date.)
                "yyyy-MM-dd HH:mm:ss.SSSZ")) "
published: false
"))))


(defn create-blog [path]
  (println "testing:" path))


(defn post [title]
  (let [url (pretty-url title)
        dir (file (:path *config*) "public" "blog"
                  url)]
    (if (.exists dir)
      ;; Directory already exists. Bail.
      (do (println "A post appears to exist at" (str "/" url) "already!")
          (System/exit 1))
      
      ;; Get this party started
      (do-generate-post-dir dir title))))
