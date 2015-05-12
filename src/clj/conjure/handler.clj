(ns conjure.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer :all]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]])
  (:gen-class))

(defroutes app-routes
  (GET  "/Ping/:data" [data] {:body data})
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      wrap-json-body
      wrap-json-params
      wrap-json-response
      (wrap-reload '(conjure.handler))))

(defn start-server [] (run-jetty (var app) {:port 8080 :join? false}))

(defn -main [& args] (start-server))
