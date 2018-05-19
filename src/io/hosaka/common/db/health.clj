(ns io.hosaka.common.db.health
  (:require [manifold.deferred :as d]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [com.stuartsierra.component :as component]
            [io.hosaka.common.db :refer [get-connection def-db-fns]]))

(def-db-fns "db/sql/health.sql")

(defn get-db-health [db]
  (d/future
    (get-db-health-sql (get-connection db))))


(defrecord Health [db env boot-time]
  component/Lifecycle

  (start [this]
    (assoc this :boot-time (l/local-now)))

  (stop [this]
    this))

(defn new-health [env]
  (component/using
   (map->Health {:env env})
   [:db]))

(defn get-health [{:keys [db env boot-time]}]
  (let [n (l/local-now)]
    (-> (get-db-health db)
        (d/chain #(if (or
                       (nil? %1)
                       (empty? %1))
                    {:health "UNHEALTHY"}
                    {:health "HEALTHY" :db %1}))
        (d/catch (fn [e] {:health "UNHEALTHY" :db (.getMessage e)}))
        (d/chain #(merge (assoc %
                                :boot-time (l/format-local-time boot-time :date-time)
                                :current-time (l/format-local-time n :date-time)
                                :up-time (t/in-seconds (t/interval boot-time n)))
                         (select-keys env [:service-id :hostname])))))
   )

