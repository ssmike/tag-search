(ns tag-search.service
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [compojure.route :as route]
            [compojure.core :refer [defroutes, GET, POST, routes]]
            [ring.middleware.defaults :refer :all]
            [tag-search.index :as index]
            [tag-search.search :as search]
            [clojure.data.json :as json]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [clojure.stacktrace]
            [clojure.java.io :as io]))


(defn handler
  [state]
  (routes
    (GET "/test" request
         (println request)
         (str request))

    (GET "/audio/:id" [id]
         (let [{by-id :by-id index :index} (:index @state)
               track (-> id Integer. by-id index :path)]
           {:status 200 :body (io/file track) :content-type "application/octet-stream"}))

    (GET "/search" req
         (json/write-str
           (search/search (:index @state)
           (-> req :params :text)
           (-> req :params (:limit 10) Integer.))))

    (route/not-found {:status 404
                      :body "Route not found!"}) ))


(defn worker
  [base delay index]
  (let [state (atom {})
        build (fn [] (swap! state assoc :index (index/build-index base)))]
    (try
      (if (and (not (nil? index)) (-> index io/file .exists))
        (let [restored-index (index/read-index base)]
          (swap! state assoc :index restored-index))
        (build))
      (catch java.lang.Exception e
        (clojure.stacktrace/print-stack-trace e)
        (build)))
    {:state state
     :worker (future
               (try
                 (while true
                   (Thread/sleep (* 1000 delay)))
                   (build)
                   (log/info "base rebuilt")
                   (log/debug (.concat "writing to built index to " index))
                   (index/write-index index (:index @state))
                 (catch java.lang.Exception e
                    (clojure.stacktrace/print-stack-trace e))))}))


(def cli-options
  [["-p" "--port PORT" "Port number"
    :id :port
    :parse-fn #(Integer/parseInt %)
    :default 80]
   ["-d" "--delay DELAY" "Builder delay in seconds"
    :required true
    :id :delay
    :parse-fn #(Integer/parseInt %)]
   ["-b" "--base BASE_PATH"
    :id :base
    :parse-fn str
    :default "."]
   ["-i" "--index INDEX_FILE"
    :id :index
    :default nil]])

(defn run-server
  [{state :state} port]
  (jetty/run-jetty (wrap-defaults (handler state) api-defaults)
                     {:port port :join? false}))

(defn -main
  [& args]
  (let [{port :port delay :delay base :base index :index} (:options (parse-opts args cli-options))
        worker (worker base delay index)]
    (run-server worker port)))

(comment
   (def *worker (worker "/home/ssmike/Downloads" 2000))
   (def srv (run-server *worker 9090))
   (.stop srv)
  )
