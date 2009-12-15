(ns blogjure
  (:use compojure
        [blogjure servlets]))

(run-server {:port 8080}
            "/*" (servlet *blog-routes*))
