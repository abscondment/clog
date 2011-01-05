(defproject clog "0.5"
  :repositories {;; markdownj
                 "scala-tools" "http://scala-tools.org/repo-releases/"
                 ;; pallet/jclouds
                 "sonatype-snapshot" "https://oss.sonatype.org/content/repositories/snapshots/"
                 "sonatype" "https://oss.sonatype.org/content/repositories/releases"}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [enlive "1.0.0-SNAPSHOT"]
                 [clj-glob "1.0.0"]
                 [clj-yaml "0.3.0-SNAPSHOT"]
                 [org.markdownj/markdownj "0.3.0-1.0.2b4"]
                 [org.cloudhoist/pallet "0.4.0-SNAPSHOT"]
                 [org.jclouds/jclouds-aws "1.0-beta-8"]
                 [org.jclouds/jclouds-blobstore "1.0-beta-8"]
]
  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT"]]
  :main clog.core)

(comment

  (use 'pallet.blobstore)

  (def bs
       (blobstore-from-map
        {:blobstore-provider "s3"
         :blobstore-identity "<identity>" :blobstore-credential "<secret>"}))
  (put-file bs "tbdo-clojuretest" "public/project.clj" "/home/brendan/code/clog/project.clj")
  (org.jclouds.blobstore/blob-exists? "tbdo-clojuretest" "public" bs)
  (org.jclouds.blobstore/blob-exists? "tbdo-clojuretest" "public/project.clj" bs)

  )