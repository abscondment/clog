(ns clog.core
  (:use [clojure.contrib.duck-streams]
        [clog config db views])
  (:gen-class))

(defn -main [& args]
  (let [posts
        (loop [return-posts (list)
               [post & more-posts :as posts] (all-posts)]
          (if (empty? posts) (reverse return-posts)
              (let [updated (or (post :updated)
                                (and (first return-posts)
                                     ((first return-posts) :updated))
                                (and (first more-posts)
                                     ((first more-posts) :updated)))
                    _ (if updated (println "Updating" (post :url)))
                    _ (if updated
                        (let [dir (java.io.File. (str "./public/blog/" (post :url)))
                              _ (if (not (.exists dir)) (.mkdir dir))
                              _ (spit (str "./public/blog/"
                                           (post :url)
                                           "/index.html")
                                      (blog-post post
                                                 (first more-posts)
                                                 (first return-posts)))
                              _ (spit (str "./public/blog/"
                                           (post :url)
                                           "/post.markdown.md5sum")
                                      (post :md5))]))]
                (recur (conj return-posts post) more-posts))))]
    (do
      (spit "./public/index.html" (blog-index posts))
      (spit "./public/sitemap.txt" (sitemap-txt posts))
      (spit "./public/sitemap.xml" (sitemap-xml posts))
      (spit "./public/blog/atom.xml" (atom-xml (take 20 posts)))
      (shutdown-agents))))

