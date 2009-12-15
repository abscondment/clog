(ns blogjure.servlets
  (:use compojure
        [blogjure db html]))

(defn show-post [posturl]
  (html-doc "test"
            (let [post (post-by-url posturl)]
              (html [:p [:h3 (post :title)]]
                    [:p (post :body)]))))

(defn list-posts []
     (html-doc "Recent Posts"
               (html (map (fn [post] [:p (link-to-post post)])
                      (top-n-posts 5)))))

(defroutes *blog-routes*
  (GET "/"
       (list-posts))
  (GET "/posts/:url"
       (show-post (params :url)))
  (GET "/*"
       (or (serve-file (params :*)) :next))    
  (ANY "*"
       (page-not-found)))