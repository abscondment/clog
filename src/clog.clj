(set! *warn-on-reflection* true)
(ns clog
  (:use [clojure.contrib duck-streams]
        [clog config db views])
  (:gen-class))

(defn -main [& args]
  (let [posts (loop [link-to '()
                     [post & more-posts :as posts] (all-posts)]
                (if (empty? posts) link-to
                    (let [dirname (str "./public/blog/" (post :url))
                          dir (java.io.File. dirname)
                          _ (if (not (.exists dir)) (.mkdir dir))
                          filename (str dirname "/index.html")
                          file (java.io.File. filename)
                          _ (if (.exists file) (.delete file))
                          _ (spit filename (blog-post post))]
                      (recur (conj link-to post) more-posts))))]
    (do
      (spit "./public/index.html" (blog-index posts))
      (spit "./public/blog/atom.xml" (atom-xml posts)))))
