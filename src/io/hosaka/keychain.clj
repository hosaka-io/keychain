(ns io.hosaka.keychain
  (:require [config.core :refer [env]]
            [com.stuartsierra.component :as component]
            [manifold.deferred :as d]
            [clojure.tools.logging :as log]
            [io.hosaka.keychain.handler :refer [new-handler]]
            [io.hosaka.keychain.keys :refer [new-keys]]
            [io.hosaka.keychain.orchestrator :refer [new-orchestrator]]
            [io.hosaka.common.db :refer [new-database]]
            [io.hosaka.common.db.health :refer [new-health]]
            [io.hosaka.common.server :refer [new-server]]
            )
  (:gen-class))

(defn init-system [env]
  (component/system-map
   :keys (new-keys)
   :handler (new-handler)
   :health (new-health env)
   :db (new-database "keychain" env)
   :orchestrator (new-orchestrator)
   :server (new-server env)))

(defonce system (atom {}))

(defn -main [& args]
  (let [semaphore (d/deferred)]
    (reset! system (init-system env))

    (swap! system component/start)
    (log/info "Keychain service booted")
    (deref semaphore)
    (log/info "keychain going down")
    (component/stop @system)

    (shutdown-agents)
    ))

(comment
  (use 'io.hosaka.keychain :reload)

  (reset! system (init-system config.core/env))

  (swap! system component/start)

  @(io.hosaka.keychain.keys/get-key (:keys @system) "0a603dd2-e63e-403e-833b-0b01fe212a9d")

  )

