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

(defn- do-make-index [{current :current
                       prev :prev
                       next :next
                       posts :posts}]
  (let [index-url #(list-to-url (cons (:path *config*) (cons "public" %)))
        dir (java.io.File. (index-url current))]
    (do (if (not (.exists dir)) (.mkdir dir))
        (spit
         (file (index-url current) "index.html")
         (apply str (views/index posts (list-to-url prev) (list-to-url next)))))))

(defn- update-indexes [posts]
  (loop [index-hashes
         (let [pages (vec (partition (:page-size *config*) posts))
               last-page (count pages)
               page-numbers (range last-page)
               page-urls (vec
                          (map #(filter
                                 identity
                                 (if (> % 0)
                                   (list "blog" "page" %)))
                               page-numbers))]    
           (for [i page-numbers]
             (merge {:current (nth page-urls i)
                     :posts (nth pages i)}
                    (if (> i 0) {:next (nth page-urls (dec i))})
                    (if (< i (dec last-page))
                      {:prev (nth page-urls (inc i))}))))]
    (if (not (empty? index-hashes))
      (do
        (do-make-index (first index-hashes))
        (recur (rest index-hashes))))))

(defn -main
  ([] (if (empty? *command-line-args*)
        (println "TODO: usage")
        (apply -main *command-line-args*)))
  ([& args]
     (do
       ;; read config
       (try
         (do-read-config (first args))
         (catch java.io.FileNotFoundException e
           (do (println "TODO: error message")
               (System/exit 1))))
       
       ;; make some templates
       (views/build-templates)
       ;; process posts
       (let [posts (db/all-posts)
             by-month (group-by-month posts)
             month-urls (map #(str "public/" %)
                             (reverse (sort (keys by-month))))]
         (do
           (update-posts posts)
           
           (println "Generating indexes.")
           (update-indexes posts)
           (let [all (file (:path *config*)  "public" "blog" "page" "all")]
             (do (if (not (.exists all)) (.mkdir all))
                 (spit (file (.getCanonicalPath all) "index.html")
                       (apply str (views/list-all-posts posts)))))
           
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
