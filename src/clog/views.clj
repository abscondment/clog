(ns clog.views
  (:use [net.cgrand.enlive-html :only [append after clone-for content deftemplate do-> get-resource html-content html-snippet last-child prepend set-attr]]
        [clojure.contrib.str-utils :only [re-gsub]]
        [clog config helpers]))

(defn- load-snippet [path] (html-snippet (get-resource path slurp)))

;; snippets that we'll reuse.
(def head (load-snippet "head.snippet"))
(def menu (load-snippet "menu.snippet"))
(def footer (load-snippet "footer.snippet"))

(def header [{:tag :a :attrs {:href "/brendan/"} :content "Brendan"}
             (html-snippet " &raquo; " (*config* :title))])
(def current-year (str (.get (java.util.Calendar/getInstance) java.util.Calendar/YEAR)))

(defn- group-by-year [posts]
  (group-by #(apply str (take 4 (% :created_at))) posts))

(defn- list-posts [posts]
  (clone-for [{:keys [title url created_at]} posts]
             [:a] (do-> (content title)
                        (set-attr :href (url-for-post url))
                        (after (str " - " created_at)))))

(defn- years-and-posts [posts]
  (let [posts (group-by-year posts)]
    (clone-for [[year selected] posts]
                      [:div.year :b] (content year)
                      [:ul.posts :li.post] (list-posts selected))))



(deftemplate index "index.template" [posts]
  [:head] (append head)
  [:div.footer] (content footer)
  [:#menu] (content menu)
  [:div.header] (content header)
  [:div.content :div :div.years] (years-and-posts posts)
  [:.currentYear] (content current-year))



(deftemplate blog-post "post.template" [post prev-post next-post]
  [:head] (append head)
  [:head :title] (content (html-snippet (str (post :title) " - Brendan Ribera")))
  [:div.footer] (content footer)
  [:#menu] (content menu)
  [:div.header] (content [(first header)
                                 (html-snippet (str " &raquo; " (post :title)))])
  [:div.content] (prepend
                         {:tag :h1  :content (html-snippet (post :title))}
                         (html-snippet @(post :body)))
  [:#previousPost] (if prev-post
                     (content
                      (html-snippet "&laquo;&nbsp;")
                      (link-to-post prev-post)))
  [:#nextPost] (if next-post
                 (content
                  (link-to-post next-post)
                  (html-snippet "&nbsp;&raquo;")))
  [:.currentYear] (content current-year))



(deftemplate atom-xml "atom.template" [posts]
  [:feed :> :title] (content (*config* :title))
  [:feed :> :updated] (content (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss-08:00")
                                               (java.util.Date.)))
  [:feed :> :entry] (clone-for
                     [{:keys [title url body created_at]} posts]
                     [:title] (content title)
                     [:link] (set-attr :href (full-url (url-for-post url))
                                              :rel "alternate"
                                              :type "text/html")
                     [:id] (content "tag:" (*config* :domain) ","
                                           (apply str (take 10 created_at)) ":/brendan/blog/" url)
                     [:updated] (content
                                 (apply str (take 10 created_at)) "T" (apply str (drop 11 created_at)) "-08:00")
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



(deftemplate sitemap-xml "sitemap.template" [posts]
  [:urlset :> last-child] (after
                           (interleave
                            (repeat "\n")
                            (map (fn [{:keys [url]}]
                                   {:tag :url
                                    :content
                                    [{:tag :loc :content (full-url (url-for-post url))}
                                     {:tag :changefreq :content "monthly"}
                                     {:tag :priority :content "1.0"}]})
                                 posts))))



(defn sitemap-txt [posts]
  (apply str
   (concat
    (map #(str (full-url %) "\n") (*config* :static-paths))
    (map #(str (full-url (url-for-post %)) "\n") posts))))
