(ns grube-api.main
  (:gen-class)
  (:require [grube-api.web :as web]
            [org.httpkit.server :as server]))

(defn -main [& args]
  (println "Server starting...")
  (web/start-websocket)
  (web/start-router)
  (server/run-server #'web/app {:port 8080}))
