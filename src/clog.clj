;(set! *warn-on-reflection* true)
(ns clog
  (:use [clojure.contrib.duck-streams]
        [clog config db views])
  (:gen-class))

(defn -main [& args]
  
   (comment
    (spit "./public/blog/posts.yaml"
          (loop [yaml "---\n"
                 [post & more-posts :as posts] (all-posts)]
            (if (empty? posts) yaml
                (let [_ (spit (str "./public/blog/" (post :url) ".markdown")
                              (post :body))]
                  (recur
                   (str yaml
                        (post :url) ":\n"
                        "  title: " (post :title) "\n"
                        "  created_at: " (post :created_at) "\n")
                   more-posts))))))
 
  (let [posts
        (loop [return-posts (list)
               [post & more-posts :as posts] (all-posts)]
          (if (empty? posts) (reverse return-posts)
              (let [dir (java.io.File. (str "./public/blog/" (post :url)))
                    _ (if (not (.exists dir)) (.mkdir dir))
                    _ (spit (str "./public/blog/" (post :url) "/index.html")
                            (blog-post post
                                       (first more-posts)
                                       (first return-posts)))]
                (recur (conj return-posts post) more-posts))))]
    (do
      (spit "./public/index.html" (blog-index posts))
      (spit "./public/sitemap.txt" (sitemap-txt posts))
      (spit "./public/blog/atom.xml" (atom-xml (take 20 posts))))))
