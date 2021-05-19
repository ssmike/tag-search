(defproject tag-search "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [compojure "1.6.2"]
                 [org/jaudiotagger "2.0.3"]
                 [org.clojure/tools.cli "1.0.206"]
                 [org.clojure/data.json "2.3.0"]
                 [org.clojure/tools.logging "1.1.0"]]
  :main tag-search.service
  :aot [tag-search.service]
  :repl-options {:init-ns tag-search.service})
