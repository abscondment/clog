(ns clog.core
  (:use
   [clojure.tools.cli :only [cli]]
   [clog config]
   [clog.generate :as generate]
   [clog.update :as update]))

(defn -main
  ([] (if (empty? *command-line-args*)
        (do (apply -main ["-h"])
            (System/exit 1))))
  
  ([& args]
     (do
       ;; parse command-line options
       (with-command-line args
         "TODO: usage"
         [[config, c "Optional config file" "."]
          remaining]

         ;; read config file
         (try
           (do-read-config config)
           (catch java.io.FileNotFoundException e
             (do (println "TODO: error message")
                 (-main)
                 (System/exit 1))))

         ;; run command
         (let [[command & extras] remaining]
           (case command
                 "new" (generate/post
                        (apply str (interleave extras (repeat " "))))
                 ;; default to update
                 (update/site)))

         ;; shut down threadpool from pmap et al.
         (shutdown-agents)))))