(ns clog.views
  (:use [compojure html]
        [clojure.contrib seq-utils str-utils]
        [clog config helpers]))

(defn blog-post [post previous-post next-post]
  (html
   "<!DOCTYPE html>\n"
   [:html
    [:head
     [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]
     [:link {:rel "stylesheet"
             :type "text/css"
             :href "/brendan/css/main.css"}]
     [:title (post :title) " - Brendan Ribera"]]
    [:body
     {:onload "setTimeout('deferredLoad();', 5);"}
     [:div {:class "envelope"}
      (lbar)
      (header (post :title))
      "\n"
      [:div {:class "content"}
       [:h1 (post :title)]
       @(post :body)
       [:h4 (post :created_at)]
       [:br]
       [:div {:id "disqus_thread"}]
       [:div {:style "clear:both;margin:2em 0 1em 0;"}
        (if previous-post
          [:div {:style "float:left;"}
           "&laquo; " (link-to-post previous-post)])
        (if next-post
          [:div {:style "float:right;"}
           (link-to-post next-post) " &raquo;"])
        [:br {:style "clear:both;"}]]
       [:noscript
        [:p
         [:a
          {:href "http://disqus.com/forums/tbdo-brendan/?url=ref"}
          "View the discussion thread."]]]]
      (footer (take 4 (post :created_at)))]
     [:script {:type "text/javascript" :src "/brendan/js/blog.js"}]]]))

(defn blog-index [posts]
  (html
   "<!DOCTYPE html>\n"
   [:html
    [:head
     ; verification
     [:meta {:name "blogcatalog" :content "9BC9690896"}]
     [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]
     [:link {:rel "stylesheet"
             :type "text/css"
             :href "/brendan/css/main.css"}]
     [:link {:rel "alternate"
             :type "application/atom+xml"
             :title (*config* :blog-title)
             :href "http://feeds.feedburner.com/threebrothers/brendan"}]
     [:title (*config* :blog-title) " - Brendan Ribera"]]
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
         (reverse (group-by #(apply str (take 4 (% :created_at))) posts)))]
       [:a {:href "http://feeds.feedburner.com/threebrothers/brendan"}
        [:img {:src "/brendan/images/feed.png" :alt "Feed"}]]]
      (footer)]
     google-analytics]]))

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
        [:link {:href (str "http://threebrothers.org/brendan/blog/" (post :url) "/")
                :rel "alternate"
                :type "text/html"}]
        [:id "tag:threebrothers.org,"
             (take 10 (post :created_at))
             ":/brendan/blog/"
             (post :url)]
        [:updated (take 10 (post :created_at)) "T"
                  (drop 11 (post :created_at)) "-08:00"]
        [:summary
         "<![CDATA[\n"
         (take 325
               (re-gsub #"[\s]+" " "
                        (re-gsub #"(</?[^>]*>)" "" @(post :body)))) "...]]>\n"]
        [:content {:type "html"} "<![CDATA[\n" @(post :body) "]]>\n"]])
     posts)]))


(defn sitemap-txt [posts]
  (apply str "http://threebrothers.org/brendan/
http://threebrothers.org/brendan/about/
http://threebrothers.org/brendan/software/\n"
         (map #(str "http://threebrothers.org/brendan/blog/"
                    (% :url)
                    "/\n")
              posts)))


(defn sitemap-xml [posts]
  (let [[newer older] (split-at 20 posts)]
    (html
     "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
     [:urlset
      {:xmlns "http://www.sitemaps.org/schemas/sitemap/0.9"}
      (map (fn [m]
             [:url
              [:loc (m :url)]
              [:changefreq (or (m :changefreq) "yearly")]
              [:priority (or (m :priority) 0.25)]])
           (concat
            ; Static urls
            (list
            {:url "http://threebrothers.org/brendan/"
             :changefreq "weekly"
             :priority 1.0}
            {:url "http://threebrothers.org/brendan/about/"
             :priority 0.5}
            {:url "http://threebrothers.org/brendan/software/"
             :changefreq "monthly"
             :priority 0.75})
            
            ; Give newer posts higher priority & more updates
            (map (fn [post]
                   {:url
                    (str "http://threebrothers.org/brendan/blog/"
                         (post :url) "/")
                    :changefreq "monthly"
                    :priority 0.75})
                 newer)
            
            ; Give older posts lower priority & fewer updates
            (map (fn [post]
                   {:url
                    (str "http://threebrothers.org/brendan/blog/"
                         (post :url) "/")})
                 older)))])))


