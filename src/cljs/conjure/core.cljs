(ns ^:figwheel-always conjure.core
    (:require-macros
     [cljs.core.async.macros :as asyncm :refer (go go-loop)])
    (:require
     [reagent.core :as reagent :refer [atom]]
     [cljs.core.async :as async :refer (<! >! put! chan)]
     [taoensso.sente  :as sente :refer (cb-success?)]))


;; Sente initialization.
(defn chsk-url-fn
  "This is how we tell Sente where our server is lolz."
  [path {:as window-location :keys [protocol pathname]} websocket?]
  (let [my-host "127.0.0.1:8080"]
    (str (if-not websocket? protocol (if (= protocol "https:") "wss:" "ws:"))
      "//" my-host (or path pathname))))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" ; Note the same path as before
       {:type :auto ; e/o #{:auto :ajax :ws}
        :chsk-url-fn chsk-url-fn})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn hello-world []
  [:h1 (:text @app-state)])

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(defn sample-send []
  (chsk-send! [:some/request-id {:name "Rich Hickey" :type "Awesome"}]))
