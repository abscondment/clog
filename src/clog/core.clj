(ns clog.core
  (:use [clojure.contrib.io :only [file]]
        [clog config helpers]
        [clog.db :as db]
        [clog.views :as views]))

(defn- update [next post prev]
  (if (or (:updated post)
          (:updated prev)
          (:updated next))
    (let [url (post :url)
          dir (java.io.File. (str (:path *config*) "/public/blog/" url))]
      (do
        (if (not (.exists dir)) (.mkdir dir))
        (spit (file (:path *config*) "public" "blog" url "index.html")
              (apply str (views/blog-post post prev next)))
        (spit (file (:path *config*) "public" "blog" url "post.markdown.md5sum")
              (:md5 post))))))

(defn- update-posts [to-update]
  (let [count (count (filter :updated to-update))
        word (if (= 1 count) "post" "posts")]
    (if (> count 0)
      (do
        (println "Updating" count (str word "."))
        (doall
         (map #(apply update %)
               (partition 3 1 (concat '(nil) to-update '(nil))))))
      (println "No posts require updating."))))

(defn -main
  ([] (apply -main *command-line-args*))
  ([& args]
     (do
       ;; read config
       (do-read-config (first args))
       ;; make some templates
       (views/build-templates)
       ;; process posts
       (let [posts (db/all-posts)]
         (update-posts posts)
         (do
           (println "Generating index.")
           (spit (file (:path *config*) "public" "index.html")
                 (apply str (views/index posts)))
           
           (println "Generating atom.")
           (spit (file (:path *config*) "public" "blog" "atom.xml")
                 (add-xml
                  (wrap-atom-cdata
                   (apply str (views/atom-xml (take 20 posts))))))

           (println "Generating sitemaps.")
           (spit (file (:path *config*) "public" "sitemap.xml")
                 (add-xml (apply str (views/sitemap-xml posts))))
           (spit (file (:path *config*) "public" "sitemap.txt") (sitemap-txt posts))
           (shutdown-agents))))))
