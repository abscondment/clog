(ns clog.db
  (:use [clojure.contrib.io]
        [clj-yaml])
  (:import
   (java.io FileNotFoundException)
   (java.security NoSuchAlgorithmException MessageDigest)
   (java.math BigInteger)))

;;; From http://www.holygoat.co.uk/blog/entry/2009-03-26-1
(defn md5-sum
  "Compute the hex MD5 sum of a string."
  [#^String str]
  (let [alg (doto (MessageDigest/getInstance "MD5")
              (.reset)
              (.update (.getBytes str)))]
    (try
     (.toString (new BigInteger 1 (.digest alg)) 16)
     (catch NoSuchAlgorithmException e
       (throw (new RuntimeException e))))))

(defn all-posts []
  (let [markdown-processor (new com.petebevin.markdown.MarkdownProcessor)]
    (reverse
     (sort-by
      :created_at
      (filter
       not-empty
       (map
        (fn [post]
          (let [url (:url post)
                exisitng-md5
                (try
                 (slurp (str "./public/blog/" url "/post.markdown.md5sum"))
                 (catch FileNotFoundException e ""))
                body (slurp (str "./public/blog/" url "/post.markdown"))
                new-md5 (md5-sum body)]
            (if (not (empty? body))
              (merge
               post
               {:body (delay (.. markdown-processor (markdown body)))
                :md5 new-md5
                :updated (not= exisitng-md5 new-md5)}))))
        (parse-string
         (slurp "./public/blog/posts.yaml"))))))))
