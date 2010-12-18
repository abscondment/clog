(ns clog.db
  (:use [clojure.contrib.io :only [file]]
        [org.satta.glob :only [glob]]
        [clj-yaml.core :only [parse-string]]
        [clog config])
  (:import
   (java.io FileNotFoundException)
   (java.math BigInteger)
   (java.security NoSuchAlgorithmException MessageDigest)))

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
    (slurp (str (file (:path *config*) "public" "blog" url "post.markdown.md5sum")))
    (catch FileNotFoundException e "")))

(defn all-posts []
  (let [markdown-processor (new com.petebevin.markdown.MarkdownProcessor)
        posts-yaml (map #(let [yaml-str (slurp %)]
                           (assoc (parse-string yaml-str)
                             :yaml-md5 (md5-sum yaml-str)
                             :url (-> % .getParent file .getName)))
                        (glob
                         (str (file (:path *config*)
                                    "public" "blog" "*" "*.yaml"))))]
    (reverse
     (sort-by :created_at
      (filter not-empty
       (pmap
        (fn [post]
          (let [body (slurp
                      (str
                       (file (:path *config*)
                             "public" "blog"
                             (post :url)
                             "post.markdown")))
                new-md5 (str (:yaml-md5 post) (md5-sum body))]
            (if (not (empty? body))
              (merge post
                     {:body (delay (.. markdown-processor (markdown body)))
                      :md5 new-md5
                      :updated (not= (existing-md5-for (:url post))
                                     new-md5)}))))
        (filter :published posts-yaml)))))))
