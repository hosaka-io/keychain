(ns io.hosaka.common.keychain
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :refer [reader]]
            [aleph.http :as http]
            [manifold.deferred :as d]
            [cheshire.core :as json]
            [byte-streams :as bs]))

(defrecord Keychain [keychain-url]
  component/Lifecycle

  (start [this]
    this)

  (stop [this]
    this))

(defn new-keychain [env]
  (map->Keychain (select-keys env [:keychain-url])))

(defn parse-stream [stream]
  (with-open [rdr (reader stream)]
    (json/parse-stream rdr true)))

(defn validate [{:keys [keychain-url]} jwt]
  (->
   (http/post keychain-url {:body jwt :headers {"content-type" "text/plain"}})
   (d/chain :body parse-stream)))
