(ns clog.db
  (:use [clojure.contrib.duck-streams])
  (:require [clojure.contrib.sql :as sql]))

  
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
             :body body})))
      (partition
       3
       (rest
        (read-lines "./public/blog/posts.yaml"))))))))

(comment
(def db {:classname    "org.postgresql.Driver"
         :subprotocol  "postgresql"
         :subname (let [db-host "127.0.0.1"
                        db-port 5432
                        db-name "typo"]
                    (str "//" db-host ":" db-port "/" db-name))
         :user "brendan"
         :password ""})

(def select-translation
     "id, title, body, extended, permalink as url, published_at as created_at")

(defn sql-query [query]
  (map
   (fn [r]
     (merge r {:body ;(.. (new com.petebevin.markdown.MarkdownProcessor)
                     ;    (markdown
                          (str (r :body) (r :extended))
                     ;     ))
               :created_at (.toString (r :created_at))}))
   (sql/with-connection db
     (sql/with-query-results res query (doall res)))))

(defn all-posts []
  (sql-query
   [(str "select " select-translation " from contents order by id desc")]))
)
