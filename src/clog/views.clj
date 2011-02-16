(ns clog.views
  (:use
   [clojure.contrib.io :only [file]]
   [clojure.contrib.str-utils :only [re-gsub]]
   [net.cgrand.enlive-html :only [append after clone-for content deftemplate do-> get-resource html-content html-snippet last-child prepend set-attr]]
   [clog config helpers]))

(defn- expand-path [name] (file (str (*config* :path) "/templates/" name)))
(defn- load-snippet [path] (html-snippet (get-resource path slurp)))

(defn- list-posts [posts]
  (clone-for [{:keys [title url created_at]} posts]
             [:a] (do-> (content title)
                        (set-attr :href (url-for-post url))
                        (after [{:tag :div :content (format-date created_at "MMMM d 'at' h:mm a")}]))))

(defn- years-and-posts [posts]
  (let [posts (group-by-year posts)]
    (clone-for [[year selected] posts]
               [:div.year :b] (content year)
               [:ul.posts :li.post] (list-posts selected))))


(defn sitemap-txt [posts]
  (apply str
         (concat
          (map #(str (full-url %) "\n") (*config* :static-paths))
          (map #(str (full-url (url-for-post %)) "\n") posts))))

;;
;; Snippets & Templates that are defined at runtime.
;;

(defn build-templates []
  ;; snippets that we'll reuse.
  (def banner-upsell (load-snippet (expand-path "banner-upsell.snippet")))
  (def head (load-snippet (expand-path "head.snippet")))
  (def menu (load-snippet (expand-path "menu.snippet")))
  (def footer (load-snippet (expand-path "footer.snippet")))

  ;; Templates
  (deftemplate index (expand-path "index.template") [posts year-urls]
    [:head] (append head)
    [:head :title] (content (str (:title *config*) " - " (:author *config*)))
    [:div.footer] (content footer)
    [:#menu] (content menu)
    [:div.banner :h1.title] (content (html-snippet (*config* :title)))
    [:div.banner :h1.title] (after [{:tag :h2 :content (html-snippet (*config* :subtitle))}
                                    {:tag :p :content banner-upsell}])
    [:div.content :div :div.years] (years-and-posts posts)
    [:.currentYear] (content current-year))

  (deftemplate blog-post (expand-path "post.template") [post prev-post next-post]
    [:head] (append head)
    [:head :title] (content (html-snippet (post :title)))
    [:div.footer] (content footer)
    [:#menu] (content menu)
    [:div.banner :h1.title] (content (html-snippet (*config* :title)))
    [:div.banner :h1.title] (after [{:tag :h2 :content (html-snippet (*config* :subtitle))}
                                    {:tag :p :content banner-upsell}])
    [:div.content :h1 :a] (do-> (set-attr :href (url-for-post post))
                                (content (html-snippet (post :title))))
    [:div.content :h1] (after (html-snippet @(post :body)))
    [:#previousPost] (if prev-post
                       (content
                        (html-snippet "&laquo;&nbsp;")
                        (link-to-post prev-post)))
    [:#nextPost] (if next-post
                   (content
                    (link-to-post next-post)
                    (html-snippet "&nbsp;&raquo;")))
    [:.currentYear] (content current-year))



  (deftemplate atom-xml (expand-path "atom.template") [posts]
    [:feed :> :title] (content (*config* :title))
    [:feed :> :subtitle] (content (*config* :subtitle))
    [:feed :> :updated] (content (date-to-rfc3339 (new java.util.Date)))
    [:feed :> :entry] (clone-for
                       [{:keys [title url body created_at]} posts]
                       [:title] (content title)
                       [:link] (set-attr :href (full-url (url-for-post url))
                                         :rel "alternate"
                                         :type "text/html")
                       [:id] (content "tag:"
                                      (*config* :domain)
                                      ","
                                      (format-date created_at "yyyy-MM-dd")
                                      ;; TODO: config
                                      ":/brendan/blog/"
                                      url)
                       [:updated] (content
                                   (date-to-rfc3339 created_at))
                       [:summary] (html-content
                                   (apply str
                                          (take 325
                                                (re-gsub
                                                 #"[\s]+" " "
                                                 (re-gsub
                                                  #"(</?[^>]*>)" "" @body))))
                                   "...")
                       [:content] (do->
                                   (set-attr :type "html")
                                   (html-content @body))))



  (deftemplate sitemap-xml (expand-path "sitemap.template") [posts]
    [:urlset :> last-child] (after
                             (interleave
                              (repeat "\n")
                              (map (fn [{:keys [url]}]
                                     {:tag :url
                                      :content
                                      [{:tag :loc :content (full-url (url-for-post url))}
                                       {:tag :changefreq :content "monthly"}
                                       {:tag :priority :content "1.0"}]})
                                   posts)))))
