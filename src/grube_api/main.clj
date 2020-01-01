(ns grube-api.main
  (:gen-class)
  (:require [grube-api.web :as web]
            [org.httpkit.server :as server]))

(defn -main [& args]
  (println "Server starting...")
  (web/start-websocket!)
  (web/start-router!)
  (web/start-ticker!)
  (server/run-server #'web/handler {:port 8080})
  (println "Server running..."))
