(ns conjure.handler
  (:use [org.httpkit.server :only [run-server]])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :refer :all]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)])
  (:gen-class))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(defroutes app-routes
  (GET  "/Ping/:data" [data] {:body data})
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post                req))
  (route/not-found "Not Found"))

(defn event-msg-handler [ev-msg] (println "We got a message."))

(def app
  (-> app-routes
      wrap-json-body
      wrap-json-params
      wrap-json-response
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params
      (wrap-reload '(conjure.handler))))

(defn start-server []
  ;; The following feels gross, this is so we can rebind event-msg-handler
  ;; and not have to start/stop the router again.
  (let [handler (fn [ev-msg] (event-msg-handler ev-msg))]
    (sente/start-chsk-router! ch-chsk handler))
  ;; Start the webserver.
  (run-server (var app) {:port 8080 :join? false}))

(defn -main [& args] (start-server))
