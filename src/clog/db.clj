(ns clog.db
  (:require [clojure.contrib.sql :as sql]))

; Information about the sqlite database
(def db {:classname    "org.sqlite.JDBC"
         :subprotocol  "sqlite"
         :subname      "db/posts.db"})

(defn sql-query [query]
  (sql/with-connection db
    (sql/with-query-results res query (doall res))))

(defn post-by-url [posturl]
  (first
   (sql-query ["select * from posts where url = ?" posturl])))

(defn top-n-posts [n]
  (sql-query
   ["select * from posts order by id desc limit ?" n]))