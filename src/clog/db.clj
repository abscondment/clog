(ns clog.db
  (:use [clojure.contrib.duck-streams]))

(def *markdown-processor*
     (new com.petebevin.markdown.MarkdownProcessor))

(defn all-posts []
  (reverse
   (sort-by
    :created_at
    (filter
     not-empty
     (map
      (fn [yaml]
        (let [url (apply str (butlast (first yaml)))
              body (slurp (str "./public/blog/" url "/post.markdown"))]
          (if (not (empty? body))
            {:url url
             :title (apply str (drop 9 (fnext yaml)))
             :created_at (apply str (drop 14 (last yaml)))
             :body (.. *markdown-processor* (markdown body))})))
      (partition
       3
       (rest
        (read-lines "./public/blog/posts.yaml"))))))))
