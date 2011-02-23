(ns clog.helpers
  (:use [clojure.contrib.io :only [file]]
        [clojure.contrib.str-utils :only [re-gsub]]
        [clog config])
  (:import (java.text SimpleDateFormat)))

(defn format-date [date format]
  "Apply a formatting string to a Date."
  (.format (new SimpleDateFormat format) date))

(defn date-adjust-timezone
  "Fix the timezone portion of Java's date formatting output. 0800 becomes 08:00."
  [date]
  (let [pos (- (count date) 2)]
    (str (apply str (take  pos date))
         ":"
         (apply str (drop pos date)))))

(defn date-to-rfc3339
  "Convert a Date to its RFC3339 output."
  [date]
  (date-adjust-timezone (format-date date "yyyy-MM-dd'T'HH:mm:ss.SSSZ")))

(def current-year (str (.get (java.util.Calendar/getInstance) java.util.Calendar/YEAR)))

(defn group-by-year [posts]
  (group-by #(format-date (% :created_at) "yyyy") posts))

(defn group-by-month [posts]
  (group-by #(format-date (% :created_at) "yyyy-MM") posts))

(defn make-url [& path]
  (apply str (cons (:root-url *config*) path)))

(defn full-url? [href]
  (not (nil? (re-find #"^[^:\s]*:?//" href))))

(defn make-full-url [href]
  (let [leading-slash  (= (str (first href)) "/")
        trailing-slash (= (str (last href)) "/" java.io.File/separator)]
    (if (full-url? href)
      href
      (str "http"
           (if (:https *config*) "s")
           "://"
           (:domain *config*)
           (if leading-slash
             href
             (-> (file (:root-url *config*) "blog" "_" href)
                 (.getCanonicalPath)))
           (if (and (false? leading-slash) trailing-slash) "/")))))

(defmulti url-for-post (fn [p] (type p)))
(defmethod url-for-post String
  [p]
  (make-url "blog/" p "/"))
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

(defn list-to-url [coll]
  (if (not (nil? coll))
    (apply str
           (interleave
            (filter identity coll)
            (repeat java.io.File/separator)))))

(defn summarize [body]
  (apply str
   (take 325
    (re-gsub
     #"[\s]+" " "
     (re-gsub
      #"(</?[^>]*>)" "" body)))))