(ns io.hosaka.keychain.handler
  (:require [com.stuartsierra.component :as component]
            [io.hosaka.keychain.orchestrator :as orchestrator]
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
   (d/catch #(assoc response :body (.getMessage %) :status 400 :headers {"content-type" "text/plain"}))))

(defn get-db-health [orchestrator {:keys [response]}]
  (->
   (orchestrator/get-db-health orchestrator)
   (d/chain #(if (= (:health %1) "HEALTHY")
               (assoc response :body %1 :status 200)
               (assoc response :body %1 :status 503)))))

(defn build-routes [orchestrator]
  ["/" [
        ["keys/"
         (yada/resource {:methods {:post {:consumes "text/plain"
                                          :response (partial validate-token orchestrator)}}})]
        [["keys/" :kid]
         (yada/resource {:parameters {:path {:kid String}}
                         :methods {
                                   :get {:response (partial get-public-key orchestrator)
                                         :produces "text/plain"}}})]
        ["health"
         (yada/resource {:methods {:get {:response (partial get-db-health orchestrator)
                                         :produces "application/json"}}})]
        ]])


(defrecord Handler [orchestrator routes]
  component/Lifecycle

  (start [this]
    (assoc this :routes (build-routes orchestrator)))

  (stop [this]
    (assoc this :routes nil)))


(defn new-handler []
  (component/using
   (map->Handler {})
   [:orchestrator])
)

