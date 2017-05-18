(ns graphql-clj-bench.query-parser
  (:require [perforate.core :refer [defgoal defcase]]
            [graphql-clj.parser :as ql]))

(defgoal query-parser-performance
  "Verifying GraphQL query parser performance.")

(defcase query-parser-performance :simple-query
  []
  (ql/parse-query-document "{ a { b { c,d } } }"))

(defcase query-parser-performance :simple-query-with-explicit-operation
  []
  (ql/parse-query-document "query { a { b { c,d } } }"))

(defcase query-parser-performance :simple-query-with-explicit-name
  []
  (ql/parse-query-document "query Q { a { b { c,d } } }"))

(defcase query-parser-performance :query-with-fragment-spread
  []
  (ql/parse-query-document "{ a { b { ... fr } } } fragment fr on B { c,d }"))

(defcase query-parser-performance :query-with-inline-spread
  []
  (ql/parse-query-document "{ a { b { ... on B { c,d } } } }"))

(defcase query-parser-performance :query-with-parameterized-fields
  []
  (ql/parse-query-document "{ a { b(id: 10) { c,d } } }"))

(defcase query-parser-performance :query-with-parameterized-fields-and-variables
  []
  (ql/parse-query-document "query Q($id: ID) { a { b(id: $id) { c,d } } }"))

(def complex-query
  "{
     newestUsers { name, image },
     topUser: firstUser (sort: \"rank\", order: \"desc\") {
       name,
       projects {
         __type,
         name,
         ...Spreadsheet,
         ...Painting
       }
     }
   }

   fragment Spreadsheet on SpreadsheetProject {
     rowCount,
     columnCount
   }

   fragment Painting on PaintingProject {
     dominantColor { name, hexCode }
   }
   ")

(defcase query-parser-performance :complex-query
  []
  (ql/parse-query-document complex-query))

(defcase query-parser-performance :complex-query-instaparse-only
  []
  (ql/orig-parse-query-document complex-query))
