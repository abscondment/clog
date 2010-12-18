(ns clog.helpers
  (:use [clojure.contrib.str-utils :only [re-gsub]]
        [clog config])
  (:import (java.text SimpleDateFormat)))

(defn format-date [date format]
  "Apply a formatting string to a Date."
  (.format (new SimpleDateFormat format) date))

(defn date-to-rfc3339
  "Convert a Date to its RFC3339 output."
  [date]
  (let [almost (format-date date "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        pos (- (count almost) 2)]
    (str (apply str (take  pos almost))
         ":"
         (apply str (drop pos almost)))))

(defn full-url [relative-url]
  (str "http://" (*config* :domain) relative-url))

(defmulti url-for-post (fn [p] (type p)))
(defmethod url-for-post String
  [p] (str "/brendan/blog/" p "/"))
(defmethod url-for-post clojure.lang.IPersistentMap
  [p] (url-for-post (p :url)))

(defn add-xmlns [text]
  (->> text
       (re-gsub #"<feed>" "<feed xmlns=\"http://www.w3.org/2005/Atom\">")
       (re-gsub #"<urlset>" "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">")))

(defn add-xml [text]
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
       (add-xmlns text)))

(defn wrap-atom-cdata [text]
  (->> text
       (re-gsub #"<summary>" "<summary><![CDATA[\n")
       (re-gsub #"<content type=\"html\">" "<content type=\"html\"><![CDATA[\n")
       (re-gsub #"</summary>" "]]>\n</summary>")
       (re-gsub #"</content>" "]]>\n</content>")))

(defn link-to-post [post]
  {:tag :a
   :attrs {:href (url-for-post post)}
   :content (post :title)})