(ns io.hosaka.common.server
  (:require [yada.yada :as yada]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component])
  (:import [java.net
            InetSocketAddress]))

(defrecord Server [handler port address server]
  component/Lifecycle

  (start [this]
    (let [socket-address (InetSocketAddress. (or address "0.0.0.0") (or port 8080))
          routes (-> handler :routes)
          s (yada/listener routes {:socket-address socket-address})]
      (do
        (log/info (str "Started server listening on "  (.toString socket-address)))
        (assoc this :server s))
      ))

  (stop [this]
    ((:close server))
    (log/warn "Stopped server")
    (assoc this :server nil)))


(defn new-server [env]
  (component/using
   (map->Server (select-keys env [:port :address]))
   [:handler]))

