(defproject grube-api "0.1.0-SNAPSHOT"
  :description "Grube api for grube game"
  :url "http://github.com/ericdallo/grube-api"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure    "1.10.1"]
                 [org.clojure/core.async "0.5.527"]
                 [com.taoensso/sente     "1.15.0"]
                 [http-kit               "2.3.0"]
                 [com.keminglabs/jetty7-websockets-async "0.1.0"]
                 [ring                   "1.8.0"]
                 [ring-cors              "0.1.13"]
                 [ring/ring-defaults     "0.1.3"]
                 [environ                "1.1.0"]
                 [compojure              "1.6.1"]]
  :plugins [[lein-environ "1.1.0"]]
  :main grube-api.main)
