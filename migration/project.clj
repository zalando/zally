(defproject db-migration "0.1.0-SNAPSHOT"
  :description "Script for migrating zally DB"
  :url "http://github.com/zalando/zally"
  :license {:name "MIT License"
            :url ""}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.postgresql/postgresql "42.2.12"]
                 [seancorfield/next.jdbc "1.0.424"]]
  :main ^:skip-aot db-migration.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
