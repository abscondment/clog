(ns clog.helpers
  (:use [hiccup]
        [clojure.contrib duck-streams]
        [clog db]))

(defn link-to-post [post]
  (html
   [:a {:href (str "/posts/" (post :url))}
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
     [:li [:a {:href "/" :class "selected"} "Blog"]]
     [:li [:a {:href "/software"} "Software"]]]]))

(defn header [& levels]
  (html
   [:div {:class "header"}
    [:a {:href "/"} "Brendan"]
    (if (empty? levels) " &raquo; Blog")]))

(defn html-doc
  [title & body]
  (html
   "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"
   \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"
   [:html {:xmlns "http://www.w3.org/1999/xhtml"}
    [:head
     [:link {:rel "stylesheet" :type "text/css" :href "/css/main.css"}]
     [:title "Brendan Ribera - " title]]
    [:body
     [:div {:class "envelope"}
      (lbar)
      (header)
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
        body ["Recent Posts"
              (vec
               (cons
                :ul
                (map
                 (fn [post]
                   [:li
                    [:a {:href (str "/blog/" (post :url))} (post :title)]
                    " - " (post :created_at)])
                 posts)))]]
    (spit "./public/index.html"
          (html-doc "Blog" (apply html body)))))
