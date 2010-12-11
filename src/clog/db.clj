(ns clog.db
  (:use [clojure.contrib.io :only [file]]
        [org.satta.glob :only [glob]]
        [clj-yaml.core :only [parse-string]])
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

(defn existing-md5-for [url]
  (try
    (slurp (str (file "public" "blog" url "post.markdown.md5sum")))
    (catch FileNotFoundException e "")))

(defn all-posts []
  (let [markdown-processor (new com.petebevin.markdown.MarkdownProcessor)
        posts-yaml (pmap #(assoc (-> % slurp parse-string)
                            :url (-> % .getParent file .getName))
                         (glob (str (file "public" "blog" "*" "*.yaml"))))
        _ (println (first (doall posts-yaml)))]
    (reverse
     (sort-by :created_at
              (filter not-empty
                      (map
                       (fn [post]
                         (let [body (slurp
                                     (str (file "public" "blog" (post :url) "post.markdown")))
                               new-md5 (md5-sum body)]
                           (if (not (empty? body))
                             (merge post
                                    {:body (delay (.. markdown-processor (markdown body)))
                                     :md5 new-md5
                                     :updated (not= (existing-md5-for (post :url))
                                                    new-md5)}))))
                       posts-yaml))))))
