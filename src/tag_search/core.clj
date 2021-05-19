(ns tag-search.core
  (:require [ring.adapter.jetty :as jetty]
            [compojure.route :as route]
            [compojure.core :refer [defroutes, GET, POST, routes]]
            [ring.middleware.defaults :refer :all]
            [tag-search.index :as index]
            [tag-search.search :as search]
            [clojure.data.json :as json]
            [clojure.tools.cli :refer [parse-opts]]))


(defn handler
  [state]
  (routes
    (GET "/test" request
         (println request)
         (str request))

    (GET "/search" req
         (json/write-str
           (search/search (:index @state)
           (-> req :params :text)
           (-> req :params (:limit 10) Integer.))))

    (route/not-found {:status 404
                      :body "Route not found!"}) ))


(defn worker
  [base delay]
  (let [state (atom {})]
    {:state state
     :worker (future
               (while true
                 (swap! state assoc :index (index/build-index base))
                 ; seconds
                 (Thread/sleep (* 1000 delay))))}))


(def cli-options
  [["-p" "--port PORT" "Port number"
    :id :port
    :parse-fn #(Integer/parseInt %)
    :default 80]
   ["-d" "--delay DELAY" "Builder delay in seconds"
    :required true
    :id :delay
    :parse-fn #(Integer/parseInt %)]
   ["-b" "--base BASE PATH"
    :id :base
    :parse-fn str
    :default "."]])

(defn -main
  [& args]
  (let [{port :port delay :delay base :base} (:options (parse-opts args cli-options))
        worker (worker base delay)]
    (jetty/run-jetty (wrap-defaults (handler (:state worker)) api-defaults)
                     {:port port :join? false})))

(comment
  (
   (def srv (-main  "-d" "300" "-p" "9999" "-b" "/home/ssmike/Downloads"))
   (.stop srv))

  )
