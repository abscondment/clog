(ns clog.views
  (:use [compojure html]
        [clojure.contrib seq-utils str-utils]
        [clog config helpers]))

(defn blog-post [post]
  (html
   "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"
   \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
   [:html {:xmlns "http://www.w3.org/1999/xhtml"}
    [:head
     [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]
     [:link {:rel "stylesheet"
             :type "text/css"
             :href "/brendan/css/main.css"}]
     [:title "Brendan Ribera - " (post :title)]]
    [:body
     [:div {:class "envelope"}
      (lbar)
      (header (post :title))
      [:div {:class "content"}
       [:h1 (post :title)]
       (post :body)
       [:br]
       [:div {:id "disqus_thread"}]
       [:script {:type "text/javascript" :src "http://disqus.com/forums/tbdo-brendan/embed.js"}]
       [:noscript
        [:p
         [:a {:href "http://disqus.com/forums/tbdo-brendan/?url=ref"} "View the discussion thread."]]]]
      (footer (take 4 (post :created_at)))]
     [:script {:type "text/javascript" :src "/brendan/js/comments.js"}]]]))

(defn blog-index [posts]
  (html
   "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"
   \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
   [:html {:xmlns "http://www.w3.org/1999/xhtml"}
    [:head
     [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]
     [:link {:rel "stylesheet"
             :type "text/css"
             :href "/brendan/css/main.css"}]
     [:link {:rel "alternate"
             :type "application/atom+xml"
             :title (*config* :blog-title)
             :href "http://feeds.feedburner.com/threebrothers/brendan"}]
     [:title "Brendan Ribera - " (*config* :blog-title)]]
    [:body
     [:div {:class "envelope"}
      (lbar)
      (header (*config* :blog-title))
      [:div {:class "content"}
       [:div
        "Recent posts:"
        (map
         (fn [year-posts]
           [:div {:style "margin-top:10px;"}
            [:b (first year-posts)]
            [:ul {:class "list"}
             (map #(vector :li (link-to-post %) " - " (% :created_at))
                  (reverse (sort-by :created_at (last year-posts))))]])
         (reverse (group-by #(apply str (take 4 (% :created_at))) posts)))]]
      (footer)]]]))

(defn atom-xml [posts]
  (html
   "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
   [:feed {:xmlns "http://www.w3.org/2005/Atom"}
    [:title (*config* :blog-title)]
    [:subtitle "Three Planes of Thought Which to Sail"]
    [:link {:href "http://threebrothers.org/brendan/blog/atom.xml"
            :rel "self"}]
    [:link {:href "http://threebrothers.org/brendan/"}]
    [:id "tag:threebrothers.org,2010-01-07:/brendan/blog"]
    [:updated (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss-08:00")
                       (java.util.Date.))]
    [:author
     [:name "Brendan Ribera"]
     [:email "brendan.ribera+blogatom@gmail.com"]]
    (map
     (fn [post]
       [:entry
        [:title (post :title)]
        [:link {:href (str "http://threebrothers.org/brendan/blog/" (post :url))
                :rel "alternate"
                :type "text/html"}]
        [:id "tag:threebrothers.org,"
             (take 10 (post :created_at))
             ":/brendan/blog/"
             (post :url)]
        [:updated (take 10 (post :created_at)) "T"
                  (drop 11 (post :created_at)) "-08:00"]
        [:summary
         (take 325
               (re-gsub #"[\s]+" " "
                        (re-gsub #"(</?[^>]*>)" "" (post :body)))) "..."]
        [:content {:type "html"} "<![CDATA[\n" (post :body) "]]>\n"]])
     posts)]))