(ns clog.helpers
  (:use [hiccup]
        [clojure.contrib duck-streams str-utils]
        [clog db]))

(defn link-to-post [post]
  (html
   [:a {:href (str "/brendan/blog/" (post :url))}
    (post :title)]))

(defn footer []
  (html
   [:div {:class "footer"}
    "Copyright &copy; Brendan Ribera " (.get (java.util.Calendar/getInstance) java.util.Calendar/YEAR) ". This work is licensed under a "
    [:a {:rel "license nofollow"
         :href "http://creativecommons.org/licenses/by-sa/3.0/us/"}
     "Creative Commons Attribution-Share Alike 3.0 United States License"]
    ". "
    [:a {:rel "license nofollow"
         :href "http://creativecommons.org/licenses/by-sa/3.0/us/"}
     [:img {:alt "Creative Commons License"
            :style "border-width:0"
            :src "http://i.creativecommons.org/l/by-sa/3.0/us/80x15.png"}]]]))

(defn lbar []
  (html
   [:div {:class "lbar"}
    [:ul {:class "menu"}
     [:li [:a {:href "/brendan/" :class "selected"} "Blog"]]
     [:li [:a {:href "/brendan/about"} "About"]]
     [:li [:a {:href "/brendan/software"} "Software"]]
     ]]))

(defn header [& levels]
  (html
   [:div {:class "header"}
    [:a {:href "/brendan/"} "Brendan"]
    (if (not (empty? levels))
      (interleave (repeat " &raquo; ") levels))]))

(defn html-doc
  [title & body]
  (html
   "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"
   \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
   [:html {:xmlns "http://www.w3.org/1999/xhtml"}
    [:head
     [:link {:rel "stylesheet" :type "text/css" :href "/brendan/css/main.css"}]
     [:link {:rel "alternate" :type "application/atom+xml" :title "Brendan Ribera's Blog" :href "http://feeds.feedburner.com/threebrothers/brendan"}]
     [:title "Brendan Ribera - " title]]
    [:body
     [:div {:class "envelope"}
      (lbar)
      (header "Blog" "Test")
      [:div {:class "content"}
       [:div body]]
      (footer)]]]))

(defn generate-all []
  (let [posts (loop [link-to '()
                     [post & more-posts :as posts] (all-posts)]
                (if (empty? posts)
                  link-to
                  (let [dirname (str "./public/blog/" (post :url))
                        dir (java.io.File. dirname)
                        _ (if (not (.exists dir)) (.mkdir dir))
                        filename (str dirname "/index.html")
                        file (java.io.File. filename)
                        _ (if (.exists file)
                            (.delete file))
                        _ (spit filename
                                (html-doc
                                 (post :title)
                                 (apply html
                                        [[:h1 (post :title)]
                                         (post :body)])))]
                    (recur (conj link-to post) more-posts))))
        body ["Recent posts:"
              (vec
               (cons
                :ul
                (map
                 (fn [post]
                   [:li
                    [:a {:href (str "/brendan/blog/" (post :url))} (post :title)]
                    " - " (post :created_at)])
                 posts)))]]
    (do
      (spit "./public/index.html"
            (html-doc "Blog" (apply html body)))
      (spit "./public/blog/atom.xml"
            (html
             "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
             [:feed {:xmlns "http://www.w3.org/2005/Atom"}
              [:title "Brendan Ribera's Blog"]
              [:link {:href "http://threebrothers.org/brendan/blog/atom.xml"
                      :rel "self"}]
              [:link {:href "http://threebrothers.org/brendan/"}]
              [:id "tag:threebrothers.org,2010-01-07:/brendan/blog"]
              [:updated (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss-08:00") (java.util.Date.))] ; .SSSZ
              [:author
               [:name "Brendan Ribera"]
               [:email "brendan.ribera+blogatom@gmail.com"]]
              (map
               (fn [post]
                 [:entry
                  [:title (post :title)]
                  [:link {:href (str "http://threebrothers.org/brendan/blog/"
                                     (post :url))
                          :rel "alternate"
                          :type "text/html"}]
                  [:id "tag:threebrothers.org," (take 10 (post :created_at)) ":/brendan/blog/" (post :url)]
                  [:updated (take 10 (post :created_at)) "T" (drop 11 (post :created_at)) "-08:00"]
                  [:summary
                   (take 300
                         (re-gsub #"[\s]+" " "
                                  (re-gsub #"(</?[^>]*>)" "" (post :body)))) "..."]
                  [:content {:type "html"} "<![CDATA[\n" (post :body) "]]>\n"]
                  ])
               posts)
              ])))))
             