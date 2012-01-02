(ns clog.core
  (:use
   [clojure.tools.cli :only [cli]]
   [clog config]
   [clog.generate :as generate]
   [clog.update :as update]))

(defn -main []
  ;; parse command-line options
  
  (let [[options remaining help]
        (cli (if (empty? *command-line-args*)
               ["-h"]
               *command-line-args*)
             ["-h" "--help" "Show help" :default false :flag true]
             ["-c" "--config" "Optional config file" :default "."])]

    (when (:help options)
      ;; print help and quit
      (println help)
      (System/exit 0))

    (do
      ;; read config file
      (try
        (do-read-config (:config options))
        (catch java.io.FileNotFoundException e
          (do (println "TODO: error message")
              (println help)
              (System/exit 1))))
         
      ;; run command
      (let [[command & extras] remaining]
        (case command
          "new" (generate/post
                 (apply str (butlast (interleave extras (repeat " ")))))
                               
          ;; default to update
          (update/site)))
         
      ;; shut down threadpool from pmap et al.
      (shutdown-agents))))

