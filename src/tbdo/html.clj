(ns blogjure.html
  (:use compojure))

(defn link-to-post [post]
  (html
   [:a {:href (str "/posts/" (post :url))}
    (post :title)]))

(defn footer []
  (html
   [:div {:class "footer"}
    "Copyright &copy; Brendan Ribera 2009. This work is licensed under a "
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
     [:li [:a {:href "software"} "Software"]]]]))

(defn header []
  (html
   [:div {:class "header"}
    [:a {:href "/"} "Revelation"]]))

(defn html-doc
  [title & body]
  (html
   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
   (doctype :xhtml-strict)
   [:html {:xmlns "http://www.w3.org/1999/xhtml"}
    [:head
     (include-css "/css/main.css")
     [:title title]]
    [:body
     [:div {:class "envelope"}
      (lbar)
      (header)
      [:div {:class "content"}
       [:h1 "Revelation"]
       [:div body]]
      (footer)]]]))