(ns clog.helpers
  (:use [hiccup]
        [clog config]))

(defn link-to-post [post]
  (html
   [:a {:href (str "/brendan/blog/" (post :url))}
    (post :title)]))

(defn footer
  ([] (footer (.get (java.util.Calendar/getInstance) java.util.Calendar/YEAR)))
  ([year]
     (html
      [:div {:class "footer"}
       "Copyright &copy; Brendan Ribera " year ". This work is licensed under a "
       [:a {:rel "license nofollow"
            :href "http://creativecommons.org/licenses/by-sa/3.0/us/"}
        "Creative Commons Attribution-Share Alike 3.0 United States License"]
       ". "
       [:a {:rel "license nofollow"
            :href "http://creativecommons.org/licenses/by-sa/3.0/us/"}
        [:img {:alt "Creative Commons License"
               :style "border-width:0"
               :src "/brendan/images/cc_by-sa_80x15.png"}]]])))

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
