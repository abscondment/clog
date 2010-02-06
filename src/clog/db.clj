(ns clog.db
  (:use [clojure.contrib.duck-streams])
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
        (fn [yaml]
          (let [url (apply str (butlast (first yaml)))
                exisitng-md5
                (try
                 (slurp (str "./public/blog/" url "/post.markdown.md5sum"))
                 (catch FileNotFoundException e ""))
                body (slurp (str "./public/blog/" url "/post.markdown"))
                new-md5 (md5-sum body)]
            (if (not (empty? body))
              {:url url
               :title (apply str (drop 9 (fnext yaml)))
               :created_at (apply str (drop 14 (last yaml)))
               :body (lazy-seq (list (.. markdown-processor (markdown body))))
               :md5 new-md5
               :updated (not= exisitng-md5 new-md5)})))
        (partition
         3
         (rest
          (read-lines "./public/blog/posts.yaml")))))))))
