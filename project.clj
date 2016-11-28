(defproject graphql-clj-bench "0.1.0-SNAPSHOT"
  :description "Benchmarks for 'tendant/graphql-clj'."
  :url "https://github.com/xsc/graphql-clj-bench"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"
            :year 2016
            :key "mit"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [graphql-clj "0.1.20"]
                 [clojure-future-spec "1.9.0-alpha13"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [perforate "0.3.4"]]
  :plugins [[perforate "0.3.4"]]
  :perforate
  {:benchmark-paths ["src"]
   :environments
   [{:name :query-parser
     :namespaces [graphql-clj-bench.query-parser]}
    {:name :query-executor
     :namespaces [graphql-clj-bench.query-executor]}]})
