(ns clog.generate
  (:use
   [clojure.java.io :only [file]]
   [clojure.string :only [lower-case replace]]
   [clog config helpers]))

(defn post [title]
  (let [pretty-url-title ((comp
                           (partial replace #"[\\-]+$", "")
                           (partial replace #"[\\-]+", "-")
                           (partial replace #"[ \n\t]+" "-")
                           (partial replace #"[^a-zA-Z0-9]+" " ")
                           (partial replace (java.util.regex.Pattern/compile
                                             (str "[" java.io.File/separator "]+")) " ")
                           lower-case) title)
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
