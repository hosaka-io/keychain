(ns io.hosaka.keychain
  (:require [config.core :refer [env]]
            [com.stuartsierra.component :as component]
            [manifold.deferred :as d]
            [clojure.tools.logging :as log]
            [io.hosaka.keychain.db :refer [new-database]]
            [io.hosaka.keychain.handler :refer [new-handler]]
            [io.hosaka.keychain.keys :refer [new-keys]]
            [io.hosaka.keychain.orchestrator :refer [new-orchestrator]]
            [io.hosaka.common.server :refer [new-server]]
            )
  (:gen-class))

(defonce system (atom {}))

(defn init-system [env]
  (component/system-map
   :keys (new-keys)
   :handler (new-handler)
   :db (new-database env)
   :orchestrator (new-orchestrator)
   :server (new-server env)))

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

