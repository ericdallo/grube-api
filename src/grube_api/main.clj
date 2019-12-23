(ns grube-api.main
  (:gen-class)
  (:require [grube-api.web :as web]
            [org.httpkit.server :as server]))

(defn -main [& args]
  (println "Server starting...")
  (server/run-server #'web/app {:port 8080}))
