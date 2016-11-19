(ns graphql-clj-bench.scenarios.starwars
  (:require [graphql-clj.parser :as parser]
            [graphql-clj.validator :as validator]
            [clojure.core.match :as match]))

(def schema-str "enum Episode { NEWHOPE, EMPIRE, JEDI }

interface Character {
  id: String!
  name: String
  friends: [Character]
  appearsIn: [Episode]
}

type Human implements Character {
  id: String!
  name: String
  friends: [Character]
  appearsIn: [Episode]
  homePlanet: String
}

type Droid implements Character {
  id: String!
  name: String
  friends: [Character]
  appearsIn: [Episode]
  primaryFunction: String
}

type Query {
  hero(episode: Episode): Character
  human(id: String!): Human
  droid(id: String!): Droid
}

schema {
  query: Query
}")

(def luke {:id "1000"
           :name "Luke Skywalker"
           :friends ["1002" "1003" "2000" "2001"]
           :appearsIn [4 5 6]
           :homePlanet "Tatooine"})

(def vader {:id "1001"
            :name "Darth Vader"
            :friends ["1004"]
            :appearsIn [4 5 6]
            :homePlanet "Tatooine"})

(def han {:id "1002"
          :name "Han Solo"
          :friends ["1000" "1003" "2001"]
          :appearsIn [4 5 6]})

(def leia {:id "1003"
           :name "Leia Organa"
           :friends ["1000" "1002" "2000" "2001"]
           :appearsIn [4 5 6]
           :homePlanet "Alderaan"})

(def tarkin {:id "1004"
             :name "Wilhuff Tarkin"
             :friends ["1001"]
             :appearsIn [4]})

(def humanData  (atom {"1000" luke
                       "1001" vader
                       "1002" han
                       "1003" leia
                       "1004" tarkin}))

(def threepio {:id "2000"
               :name "C-3PO"
               :friends ["1000" "1002" "1003" "2001"]
               :appearsIn [4 5 6]
               :primaryFunction "Protocol"})

(def artoo {:id "2001"
            :name "R2-D2"
            :friends ["1000" "1002" "1003"]
            :appearsIn [4 5 6]
            :primaryFunction "Astromech"})

(def droidData (atom {"2000" threepio
                      "2001" artoo}))

(defn get-human [id]
  (get @humanData id))

(defn get-droid [id]
  (get @droidData id))

(defn get-character [id]
  (or (get-human id)
      (get-droid id)))

(defn get-friends [character]
  (map get-character (:friends character)))

(defn get-hero [episode]
  (if (= episode 5)
    luke
    artoo))

(def human-id (atom 2050))

(defn resolver-fn [type-name field-name]
  (match/match
    [type-name field-name]
    ["Query" "hero"] (fn [context parent args]
                       (get-hero (:episode args)))
    ["Query" "human"] (fn [context parent args]
                        (get-human (str (get args "id"))))
    ["Query" "droid"] (fn [context parent args]
                        (get-droid (str (get args "id"))))
    ;; Hacky!!! Should use resolver for interface
    ["Human" "friends"] (fn [context parent args]
                          (get-friends parent))
    ["Droid" "friends"] (fn [context parent args]
                          (get-friends parent))
    ["Character" "friends"] (fn [context parent args]
                              (get-friends parent))
    :else nil))

(def schema (validator/validate-schema (parser/parse schema-str)))
