(ns clog.generate
  (:require [clojure.string])
  (:use
   [clojure.java.io :only [file]]
   [clog config helpers]))

(defn post [title]
  (let [pretty-url-title ((comp
                           (partial clojure.string/replace #"[\\-]+$", "")
                           (partial clojure.string/replace #"[\\-]+", "-")
                           (partial clojure.string/replace #"[ \n\t]+" "-")
                           (partial clojure.string/replace #"[^a-zA-Z0-9]+" " ")
                           (partial clojure.string/replace (java.util.regex.Pattern/compile
                                             (str "[" java.io.File/separator "]+")) " ")
                           clojure.string/lower-case) title)
        dir (file (:path *config*) "public" "blog" pretty-url-title)]
    (if (.exists dir)
      ;; Directory already exists. Bail.
      (do (println "A post appears to exist at" (str "/" pretty-url-title) "already!")
          (System/exit 1))
      
      ;; Get this party started
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
"))))))
