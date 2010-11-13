(ns clog.core
  (:use [clojure.contrib.io :only [file]]
        [clog config helpers]
        [clog.db :as db]
        [clog.views :as views])
  (:gen-class))

(defn- update [next post prev]
  (if (or (:updated post)
          (:updated prev)
          (:updated next))
    (let [url (post :url)
          dir (java.io.File. (str "./public/blog/" url))]
      (do
        (if (not (.exists dir)) (.mkdir dir))
        (spit (file "public" "blog" url "index.html")
              (apply str (views/blog-post post prev next)))
        (spit (file "public" "blog" url "post.markdown.md5sum")
              (post :md5))))))

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

(defn -main [& args]
  (let [posts (db/all-posts)]
    (update-posts posts)
    (do
      (println "Generating index.")
      (spit (file "public" "index.html")
            (apply str (views/index posts)))
      
      (println "Generating atom.")
      (spit (file "public" "blog" "atom.xml")
            (add-xml
             (wrap-atom-cdata
              (apply str (views/atom-xml (take 20 posts))))))

      (println "Generating sitemaps.")
      (spit (file "public" "sitemap.xml")
            (add-xml (apply str (views/sitemap-xml posts))))
      (spit (file "public" "sitemap.txt") (sitemap-txt posts))

      (shutdown-agents))))

