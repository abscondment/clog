(ns clog.core
  (:use
   [clojure.tools.cli :only [cli]]
   [clog config]
   [clog.generate :as generate]
   [clog.update :as update]))

(defn -main []
  ;; parse command-line options
  
  (let [[options remaining help]
        (cli *command-line-args*
             ["update" "Generate any HTML that is out of date."]
             ["-h" "--help" "Show this help message" :flag true]
             ["-c" "--config" "Optional path to a config file" :default "."])]

    (when (or (:help options) (empty? remaining))
      ;; print help and quit
      (println help)
      (System/exit 0))

    (do
      ;; read config file
      (do-read-config (:config options))
         
      ;; run command
      (let [[command & extras] remaining
            extras-string (apply str (butlast (interleave extras (repeat " "))))]
        (case command
          "create-blog" (generate/create-blog (or extras-string "."))
          "new" (generate/post extras-string)
          "update" (update/site)))
         
      ;; shut down threadpool from pmap et al.
      (shutdown-agents))))

