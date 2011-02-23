(ns clog.generate
  (:use
   [clojure.contrib.io :only [file]]
   [clojure.contrib.string :only [chomp lower-case]]
   [clojure.contrib.str-utils :only [re-gsub]]
   [clog config helpers]))

(defn post [title]
  (let [pretty-url-title ((comp
                           (partial re-gsub #"[\\-]+$", "")
                           (partial re-gsub #"[\\-]+", "-")
                           (partial re-gsub #"[ \n\t]+" "-")
                           (partial re-gsub #"[^a-zA-Z]+" " ")
                           (partial re-gsub (java.util.regex.Pattern/compile
                                             (str "[" java.io.File/separator "]+")) " ")
                           lower-case) title)
        dir (file (:path *config*) "public" "blog" pretty-url-title)]
    (if (.exists dir)
      ;; Directory already exists. Bail.
      (do (println "A post appears to exist at" (str "/" pretty-url-title) "already!")
          (System/exit 1))
      
      ;; Get this party started
      (do
        (.mkdir dir)
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
