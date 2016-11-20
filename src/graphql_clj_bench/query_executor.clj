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
    human(id:\"1002\") {
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

(defgoal query-execution "Verifying GraphQL query execution overhead")

(defcase query-execution :uncached []
  (executor/execute nil s-sw/schema s-sw/resolver-fn query-str))

(defcase query-execution :cached []
  (executor/execute nil s-sw/schema s-sw/resolver-fn (prep-statement s-sw/schema query-str)))

(defcase query-execution :cached-inline-resolvers []
  (executor/execute nil s-sw/schema s-sw/resolver-fn (prep-statement s-sw/schema s-sw/resolver-fn query-str)))

(def query-str-vars-frag
  "query($id:String!) {
    human(id:$id) {
      ...IdName
      friends {
        ...IdName
        friends {
          id
        }
      }
    }
  }

  fragment IdName on Character {
    id
    name
  }")

(defgoal query-execution-vars-frag "Verifying GraphQL query execution overhead with variables and fragments")

(defcase query-execution-vars-frag :uncached []
  (executor/execute nil s-sw/schema s-sw/resolver-fn query-str-vars-frag {"id" "1002"}))

(defcase query-execution-vars-frag :cached []
  (executor/execute nil s-sw/schema s-sw/resolver-fn (prep-statement s-sw/schema query-str-vars-frag) {"id" "1002"}))

(defcase query-execution-vars-frag :cached-inline-resolvers []
  (executor/execute nil s-sw/schema s-sw/resolver-fn (prep-statement s-sw/schema s-sw/resolver-fn query-str-vars-frag) {"id" "1002"}))
