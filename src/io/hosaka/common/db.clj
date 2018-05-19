(ns io.hosaka.common.db
  (:require [hikari-cp.core :refer :all]
            [hugsql.core :as hugsql]
            [hugsql.adapter.clojure-java-jdbc :as adapter]
            [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component])
  (:import [org.flywaydb.core
            Flyway]))

(defprotocol DBConnection
  (get-connection [this]))

(defrecord Database [schema db-conf datasource]
  component/Lifecycle

  (start [this]
    (do
      (log/info "Connecting to DB")
      (let [ds (make-datasource db-conf)
            flyway (Flyway.)]
        (log/info "Connected to DB, starting migration")
        (.setSchemas flyway (into-array [schema]))
        (.setDataSource flyway ds)
        (.migrate flyway)
        (log/info "Migration complete")
        (assoc this :datasource ds))))

  (stop [this]
    (close-datasource datasource)
    (assoc this :datasource nil))

  DBConnection

  (get-connection [this] (select-keys this [:datasource]))
)

(defn new-database [schema {:keys [db-url db-user db-password]}]
  (map->Database {:schema schema :db-conf {:username db-user :password db-password :jdbc-url db-url}}))

(defn def-db-fns [f]
  (hugsql/def-db-fns f {:adapter (adapter/hugsql-adapter-clojure-java-jdbc)}))

