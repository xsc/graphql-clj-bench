(ns graphql-clj-bench.query-executor
  (:require [perforate.core :refer [defgoal defcase]]
            [graphql-clj.executor :as executor]
            [graphql-clj.parser :as parser]
            [graphql-clj.validator :as validator]
            [graphql-clj.resolver :as resolver]
            [graphql-clj-bench.scenarios.starwars :as s-sw]))

(defn- prep-statement*
  "Helper function to cache statement parsing and validation output"
  ([schema statement-str]
   (-> statement-str parser/parse (validator/validate-statement schema)))
  ([schema resolver-fn statement-str]
   (let [resolver-fn (resolver/create-resolver-fn schema resolver-fn)
         schema-w-resolver (assoc schema :resolver resolver-fn)] ;; Enable inlining resolver functions
     (prep-statement* schema-w-resolver statement-str))))
(def prep-statement (memoize prep-statement*))

(def query-str
  "query {
    human (id: \"1002\") {
      id
      name
      friends {
        id
        name
        friends {
          id
        }
      }
    }
  }")

(declare no-caching)
(defgoal no-caching "Verifying GraphQL query string parsing, validation, and execution overhead.")

(defcase no-caching :nested-query []
  (executor/execute nil s-sw/schema s-sw/resolver-fn query-str {:id "1002"}))

(declare caching)
(defgoal caching "Verifying GraphQL query execution overhead when caching query string parsing and validation.")

(defcase caching :nested-query []
  (executor/execute nil s-sw/schema s-sw/resolver-fn (prep-statement s-sw/schema query-str)))

(declare inline-resolvers)
(defgoal inline-resolvers "Verifying GraphQL query execution overhead with inline resolver functions.")

(defcase inline-resolvers :nested-query []
  (executor/execute nil s-sw/schema s-sw/resolver-fn (prep-statement s-sw/schema s-sw/resolver-fn query-str)))
