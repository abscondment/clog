(ns clog.core
  (:use [clojure.contrib.io :only [spit file]]
        [clog config db views])
  (:gen-class))

(defn update [post prev next]
  (if (or (:updated post)
          (:updated prev)
          (:updated next))
    (let [url (post :url)
          dir (java.io.File. (str "./public/blog/" url))]
      (do
        (println "Updating" url)
        (if (not (.exists dir)) (.mkdir dir))
        (spit (file "public" "blog" url "index.html")
              (blog-post post prev next))
        (spit (file "public" "blog" url "post.markdown.md5sum")
              (post :md5))))))

(defn update-posts [to-update]
  (loop [return-posts (list)
         [post & more-posts :as posts] to-update]
    (if (empty? posts) (reverse return-posts)
        (do
          (update post (first more-posts) (first return-posts))
          (recur (conj return-posts post) more-posts)))))

(defn -main [& args]
  (let [posts (update-posts (all-posts))]
    (do
      (spit (file "public" "index.html") (blog-index posts))
      (spit (file "public" "sitemap.txt") (sitemap-txt posts))
      (spit (file "public" "sitemap.xml") (sitemap-xml posts))
      (spit (file "public" "blog" "atom.xml") (atom-xml (take 20 posts)))
      (shutdown-agents))))

