(ns db-migration.core
  (:require 
    [next.jdbc :as jdbc]
    [clojure.string :refer [trim]])
  (:import
    [java.lang System])
  (:gen-class))

(def query-limit 1000)

(def api-reviews-read-count "api_review.txt")
(def rule-violation "rule_violation")
(def api-file "api.data")
(def rule-violations-data "rule-violations.data")
(def rules-read-count "rules-read.txt")
(def api-reviews-read (atom 0))
(def rule-violations-read (atom 0))


(defn table
  [table-name]
  (case table-name
          "api_review" :api-review
          "rule_violation" :rule-violation))
(defn prep
  [table-name]
  (let [table-kw (table table-name) 
        start (-> (clojure.edn/read-string  (if (= table-kw :api-review)
                                              (slurp api-reviews-read-count)
                                              (slurp rules-read-count))))
        _ (if (= table-kw :api-review)
            (swap! api-reviews-read (fn [_] start))
            (swap! rule-violations-read (fn [_] start)))]
    (println "starting at" start)))

(defn write-rule-violations
  [out-rows rows-count]
  (when (not (zero? rows-count))
    (spit rule-violations-data out-rows :append true))
  (swap! rule-violations-read (fn [prev-read] (+ prev-read rows-count)))
  (spit rules-read-count @rule-violations-read)
  (println "read " rows-count " rows. Offset is currently " @rule-violations-read))

(defn write-api-reviews
  [out-rows rows-count]
  (when (not (zero? rows-count))
    (spit rule-violations-data out-rows :append true))
  (swap! api-reviews-read (fn [prev-read] (+ prev-read rows-count)))
  (spit api-reviews-read-count @rule-violations-read)
  (println "read " rows-count " rows. Offset is currently " @api-reviews-read))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (if (< (count args) 1)
    (throw (Exception. "Too few arguments. lein run table-name")))
  (println "starting prep")
  (prep (trim  (nth args 0)))
  (let [table-name (table (trim (nth args 0)))
        connection (jdbc/get-connection  {:dbtype "postgresql"
                                       :host "127.0.0.1"
                                       :port 5432
                                       :user "zally_admin"
                                       :dbname "zally"
                                       :password (System/getenv "PG_PASSWORD")})
        query (format "select * from %s offset ? limit ?" (trim (nth args 0)))]
    (loop []
      (let [offset (if (= table-name :api-review) 
                     @api-reviews-read
                     @rule-violations-read)
            rows  (jdbc/execute! connection [query offset query-limit])
            rows-count (count rows)
            out-rows   (str "\n" (clojure.string/join "\n" (map pr-str rows)))]
        (if (= table-name :api-review)
          (write-api-reviews out-rows rows-count) 
          (write-rule-violations out-rows rows-count))
        (when (not (zero? rows-count))
          (recur))))))
