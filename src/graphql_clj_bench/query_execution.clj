(ns graphql-clj-bench.query-execution
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
  "query {\n  human (id:\"1002\") {\n    id\n    name\n    friends {\n      id\n      name\n      friends {\n        id\n      }\n    }\n  }\n}")

(declare no-precompilation)
(defgoal no-precompilation "Verifying GraphQL query string parsing, validation, and execution overhead.")

(defcase no-precompilation :nested-query []
  (executor/execute nil s-sw/starwars-schema s-sw/starwars-resolver-fn query-str))

(declare precompilation)
(defgoal precompilation "Verifying GraphQL query execution overhead for an already parsed and validated query string.")

(def valid-query (prep-statement s-sw/starwars-schema query-str))

(defcase precompilation :nested-query []
  (executor/execute nil s-sw/starwars-schema s-sw/starwars-resolver-fn valid-query))

(declare caching)
(defgoal caching "Verifying GraphQL query execution overhead when caching query string parsing and validation.")

(defcase caching :nested-query []
  (->> (prep-statement s-sw/starwars-schema query-str)
       (executor/execute nil s-sw/starwars-schema s-sw/starwars-resolver-fn)))

;(declare inline-resolvers)
;(defgoal inline-resolvers "Verifying GraphQL query execution overhead with inline resolver functions.")
;
;(defcase inline-resolvers :nested-query []
;  (->> (prep-statement s-sw/starwars-schema s-sw/starwars-resolver-fn query-str)
;       (executor/execute nil s-sw/starwars-schema s-sw/starwars-resolver-fn)))
