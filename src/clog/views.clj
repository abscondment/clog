(ns clog.views
  (:use
   [clojure.java.io :only [file]]
   [net.cgrand.enlive-html :only [append after at before clone-for content
                                  deftemplate do-> get-resource html-content
                                  html-snippet last-child prepend set-attr]]
   [clog config helpers]))

(defn- expand-path [name] (file (str (*config* :path) "/templates/" name)))
(defn- load-snippet [path] (html-snippet (get-resource path slurp)))

(defn- list-posts [posts]
  (clone-for [{:keys [title body created_at] :as post} posts]
             (do->
              (content [{:tag :h1
                         :content [{:tag :a
                                    :content title
                                    :attrs {:href (url-for-post post)}}]}
                        {:tag :div
                         :attrs {:class "date"}
                         :content (format-date created_at "MMMM d, yyyy 'at' h:mm a")}
                        {:tag :div
                         :content (html-snippet (summarize @body) "...")}]))))

(defn- years-and-posts [posts]
  (let [posts (group-by-year posts)]
    (clone-for [[year selected] posts]
               [:div.year :b] (content year)
               [:ul.posts :li.post :a]
               (clone-for
                [{:keys [title created_at] :as post} selected]
                (do-> (content title)
                      (set-attr :href (url-for-post post))
                      (after [{:tag :div
                               :attrs {:class "date"}
                               :content (format-date created_at "MMMM d 'at' h:mm a")}]))))))

(defn sitemap-txt [posts]
  (apply str
         (concat
          (map #(str (make-full-url %) "\n") (*config* :static-paths))
          (map #(str (make-full-url (url-for-post %)) "\n") posts))))

;;
;; Snippets & Templates that are defined at runtime.
;;

(defn build-templates []
  ;;
  ;; snippets that we'll reuse.
  ;;
  
  (def banner (load-snippet (expand-path "banner.snippet")))
  (def head (load-snippet (expand-path "head.snippet")))
  (def menu (load-snippet (expand-path "menu.snippet")))
  (def footer (load-snippet (expand-path "footer.snippet")))

  
  ;;
  ;; Templates
  ;;
  
  (deftemplate index (expand-path "index.template") [posts prev-url next-url]
    [:head] (append head)
    [:head :title] (content (str (:title *config*) " - " (:author *config*)))
    [:div.footer] (content footer)
    [:#menu] (content menu)
    [:div.banner :h1.title] (content (html-snippet (*config* :title)))
    [:div.banner :h1.title] (after [{:tag :h2 :content (html-snippet (*config* :subtitle))}
                                    {:tag :p :content banner}])
    [:div.content :div :ul.posts :li.post] (list-posts posts)
    
    [:div.nextLinks :div.left]
    (content
     (if prev-url
       {:tag :h3
        :content [(apply str (html-snippet "&laquo;&nbsp;"))
                  {:tag :a
                   :attrs {:href (make-url prev-url)}
                   :content "Older"}]}))
    
    [:div.nextLinks :div.middle :h3]
    (prepend {:tag :a
             :content "View all posts"
             :attrs {:href (make-url "blog/page/all/")}})
    
    [:div.nextLinks :div.right]
    (if next-url
      (content
       {:tag :h3
        :content [{:tag :a
                   :attrs {:href (make-url next-url)}
                   :content "Newer"}
                  (apply str (html-snippet "&nbsp;&raquo;"))]}))
    
    [:.currentYear] (content current-year))

  
  (deftemplate list-all-posts (expand-path "list.template") [posts]
    [:head] (append head)
    [:head :title] (content (str (:title *config*) " - " (:author *config*)))
    [:div.footer] (content footer)
    [:#menu] (content menu)
    [:div.banner :h1.title] (content (html-snippet (*config* :title)))
    [:div.banner :h1.title] (after [{:tag :h2 :content (html-snippet (*config* :subtitle))}
                                    {:tag :p :content banner}])
    [:div.content :div :div.years] (years-and-posts posts)
    [:.currentYear] (content current-year))
  

  (deftemplate blog-post (expand-path "post.template") [post prev-post next-post]
    [:head] (append head)
    [:head :title] (content (html-snippet (post :title)))
    [:div.footer] (content footer)
    [:#menu] (content menu)
    [:div.banner :h1.title] (content (html-snippet (*config* :title)))
    [:div.banner :h1.title] (after [{:tag :h2 :content (html-snippet (*config* :subtitle))}
                                    {:tag :p :content banner}])
    [:div.content :h1 :a] (do-> (set-attr :href (url-for-post post))
                                (content (html-snippet (post :title))))
    [:div.content :h1] (after [{:tag :div
                                :attrs {:class "date"}
                                :content (format-date (post :created_at) "MMMM d, yyyy 'at' h:mm a")}
                               (html-snippet @(post :body))])
    [:#previousPost] (if prev-post
                       (content
                        (html-snippet "&laquo;&nbsp;")
                        (link-to-post prev-post)))
    [:#nextPost] (if next-post
                   (content
                    (link-to-post next-post)
                    (html-snippet "&nbsp;&raquo;")))
    [:.currentYear] (content current-year))



  (deftemplate atom-xml (expand-path "atom.template") [posts start-year]
    [:feed :> :title] (content (*config* :title))
    [:feed :> :subtitle] (content (*config* :subtitle))
    [:feed :> :subtitle] (after [{:tag :link
                                  :attrs {:rel "self"
                                          :href (make-full-url (str "/" (*config* :root-path) "/atom.xml"))}}
                                 {:tag :link
                                  :attrs {:href (make-full-url (str "/" (*config* :root-path)))}}])
    [:feed :> :id] (content "tag:"
                            (*config* :domain)
                             ","
                             (format-date start-year "yyyy-MM-dd")
                             ":/"
                             (*config* :root-path))
    [:feed :> :updated] (content (date-to-rfc3339 (new java.util.Date)))
    [:feed :> :author] (append {:tag :name :content (:author *config*)})
    (if (:email *config*) [:feed :> :author]) (append {:tag :email :content (:email *config*)})
    [:feed :> :entry] (clone-for
                       [{:keys [title url body created_at] :as post} posts]
                       [:title] (content title)
                       [:link] (set-attr :href (make-full-url (url-for-post post))
                                         :rel "alternate"
                                         :type "text/html")
                       [:id] (content "tag:"
                                      (*config* :domain)
                                      ","
                                      (format-date created_at "yyyy-MM-dd")
                                      ":"
                                      (url-for-post post))
                       [:updated] (content
                                   (date-to-rfc3339 created_at))
                       [:summary] (html-content
                                   (summarize @body)
                                   "...")
                       [:content] (do->
                                   (set-attr :type "html")
                                   (html-content @body)))
    ;; update relative paths - links and images
    [:a] (at (fn [node]
               (update-in node [:attrs :href]
                          #(if (full-url? %) % (make-full-url %)))))
    [:img] (at (fn [node]
                 (update-in node [:attrs :src]
                            #(if (full-url? %) % (make-full-url %))))))

  
  (deftemplate sitemap-xml (expand-path "sitemap.template") [posts]
    [:urlset :> last-child] (after
                             (interleave
                              (repeat "\n")
                              (map (fn [post]
                                     {:tag :url
                                      :content
                                      [{:tag :loc :content (make-full-url (url-for-post post))}
                                       {:tag :changefreq :content "monthly"}
                                       {:tag :priority :content "1.0"}]})
                                   posts)))))

