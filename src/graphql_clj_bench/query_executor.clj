(ns graphql-clj-bench.query-executor
  (:require [perforate.core :refer [defgoal defcase]]
            [graphql-clj.executor :as executor]
            [graphql-clj.parser :as parser]
            [graphql-clj.query-validator :as query-validator]
            [graphql-clj.schema-validator :as schema-validator]
            [graphql-clj.resolver :as resolver]
            [graphql-clj-bench.scenarios.starwars :as s-sw]))

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

(def validated-document (query-validator/validate-query s-sw/validated-schema query-str))

(defgoal query-execution "Verifying GraphQL query execution overhead")

(defcase query-execution :parsing-only []
  (parser/parse-query-document query-str))

(defcase query-execution :uncached []
  (executor/execute nil s-sw/validated-schema s-sw/resolver-fn query-str))

(defcase query-execution :cached-schema []
  (executor/execute nil s-sw/validated-schema s-sw/resolver-fn query-str))

(defcase query-execution :cached-schema-and-cached-query []
  (executor/execute nil s-sw/validated-schema s-sw/resolver-fn validated-document))

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

(def validated-query-str-vars-frag (query-validator/validate-query s-sw/validated-schema query-str-vars-frag))

(defgoal query-execution-vars-frag "Verifying GraphQL query execution overhead with variables and fragments")

(defcase query-execution-vars-frag :parsing-only []
  (parser/parse-query-document query-str-vars-frag))

(defcase query-execution-vars-frag :uncached []
  (executor/execute nil s-sw/schema-str s-sw/resolver-fn query-str-vars-frag {"id" "1002"}))

(defcase query-execution-vars-frag :cached-schema []
  (executor/execute nil s-sw/validated-schema s-sw/resolver-fn query-str-vars-frag {"id" "1002"}))

(defcase query-execution-vars-frag :cached-inline-resolvers []
  (executor/execute nil s-sw/validated-schema s-sw/resolver-fn validated-query-str-vars-frag {"id" "1002"}))
