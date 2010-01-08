(ns clog
  (:use [clog helpers])
  (:gen-class))

(defn -main [& args]
  (generate-all))

(-main)