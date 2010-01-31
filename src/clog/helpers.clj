(ns clog.helpers
  (:use [compojure html]
        [clog config]))

(defn link-to-post [post]
  (html
   [:a {:href (str "/brendan/blog/" (post :url) "/")}
    (post :title)]))

(def google-analytics
     (html   
      [:script {:type "text/javascript"}
       "
      var gaJsHost = ((\"https:\" == document.location.protocol) ? \"https://ssl.\" : \"http://www.\");
      document.write(unescape(\"%3Cscript src='\" + gaJsHost + \"google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E\"));
"]
   [:script {:type "text/javascript"}
    "
      try {
        var pageTracker = _gat._getTracker(\"UA-12753089-1\");
        pageTracker._setDomainName(\".threebrothers.org\");
        pageTracker._trackPageview();
      } catch(err) {}
"]))

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
     [:li [:a {:href "/brendan/about/"} "About"]]
     [:li [:a {:href "/brendan/software/"} "Software"]]
     ]]))

(defn header [& levels]
  (html
   [:div {:class "header"}
    [:a {:href "/brendan/"} "Brendan"]
    (if (not (empty? levels))
      (interleave (repeat " &raquo; ") levels))]))
