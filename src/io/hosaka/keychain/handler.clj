(ns io.hosaka.keychain.handler
  (:require [com.stuartsierra.component :as component]
            [io.hosaka.keychain.orchestrator :as orchestrator]
            [clojure.tools.logging :as log]
            [io.hosaka.common.db.health :as health]
            [cheshire.core :as json]
            [manifold.deferred :as d]
            [yada.yada :as yada]))

(defn get-public-key [orchestrator ctx]
  (let [kid (-> ctx :parameters :path :kid)]
    (orchestrator/get-public-key orchestrator kid)))

(defn validate-token [orchestrator {:keys [body response]}]
  (->
   (orchestrator/validate-token orchestrator body)
   (d/chain #(assoc response :body (json/generate-string %) :status 200 :headers {"content-type" "application/json"}))
   (d/catch #(let [msg (.getMessage %)]
               (log/warn (str "Invalid token: " msg))
               (assoc response :body msg :status 400 :headers {"content-type" "text/plain"})))))


(defn get-db-health [health {:keys [response]}]
  (->
   (health/get-health health)
   (d/chain #(if (= (:health %1) "HEALTHY")
               (assoc response :body %1 :status 200)
               (assoc response :body %1 :status 503)))))

(defn get-authoritative-keys [orchestrator ctx]
  (orchestrator/get-authoritative-keys orchestrator))

(defn build-routes [orchestrator health]
  ["/" [
        ["keys"
         (yada/resource {:methods
                         {:get
                          {:produces "application/json"
                           :response (partial get-authoritative-keys orchestrator)}
                          :post
                          {:consumes "text/plain"
                           :response (partial validate-token orchestrator)}}})]
        [["keys/" :kid]
         (yada/resource {:parameters {:path {:kid String}}
                         :methods
                         {:get
                          {:response (partial get-public-key orchestrator)
                           :produces "application/json"}}})]
        ["health"
         (yada/resource {:methods
                         {:get
                          {:response (partial get-db-health health)
                           :produces "application/json"}}})]
        ]])


(defrecord Handler [orchestrator routes health]
  component/Lifecycle

  (start [this]
    (assoc this :routes (build-routes orchestrator health)))

  (stop [this]
    (assoc this :routes nil)))


(defn new-handler []
  (component/using
   (map->Handler {})
   [:orchestrator :health])
)

