(ns clog.generate
  (:require [clojure.string])
  (:use
   [clojure.java.io :only [file]]
   [clog config helpers]
   [leiningen.new templates]))

(def render (renderer "default"))

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

(defn create-blog 
  "Create a new blog."
  [name]
  (let [data {:name name
              :sanitized (sanitize name)
              :placeholder "{{sanitized}}"}]
    (println "Generating new blog.")
    (->files data
             ["config.clj" (render "config.clj" data)]
             [".gitignore" (render "gitignore" data)]
             ["public/2012/01/example-post/post.yaml" (render "example-post.yaml" data)]
             ["public/2012/01/example-post/post.markdown" (render "example-post.markdown" data)]
             ["public/page/all/.gitkeep" (render "gitkeep" data)]
             ["public/css/main.css" (render "main.css" data)]
             ["templates/atom.template" (render "atom.template" data)]
             ["templates/index.template" (render "index.template" data)]
             ["templates/list.template" (render "list.template" data)]
             ["templates/post.template" (render "post.template" data)]
             ["templates/sitemap.template" (render "sitemap.template" data)]
             ["templates/banner.snippet" (render "banner.snippet" data)]
             ["templates/footer.snippet" (render "footer.snippet" data)]
             ["templates/head.snippet" (render "head.snippet" data)]
             ["templates/menu.snippet" (render "menu.snippet" data)]
             ["templates/post-footer.snippet" (render "post-footer.snippet" data)])))


(defn post [title]
  (let [url (pretty-url title)
        dir (file (:path *config*) "public" current-year current-month url)]
    (if (.exists dir)
      ;; Directory already exists. Bail.
      (do (println "A post appears to exist at" (str "/" url) "already!")
          (System/exit 1))
      
      ;; Get this party started
      (do-generate-post-dir dir title))))
