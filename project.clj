(defproject io.hosaka/keychain "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories ^:replace [["releases" "https://artifactory.i.hosaka.io/artifactory/libs-release"]
                           ["snapshots" "https://artifactory.i.hosaka.io/artifactory/libs-snapshot"]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 [org.clojure/spec.alpha "0.1.143"]

                 [io.hosaka/common "1.0.0"]
                 [org.apache.logging.log4j/log4j-core "2.11.0"]
                 [org.apache.logging.log4j/log4j-api "2.11.0"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.11.0"]

                 [yogthos/config "1.1.1"]

                 [buddy "2.0.0"]
                 [buddy/buddy-sign "3.0.0.x"]
                 [buddy/buddy-core "1.5.0.x"]
                 [clj-crypto "1.0.2"
                  :exclusions [org.bouncycastle/bcprov-jdk15on bouncycastle/bcprov-jdk16]]

                 [ring/ring-core "1.6.2"]
                 [ring/ring-defaults "0.3.1"]

                 [org.postgresql/postgresql "42.2.2"]

                 [com.novemberain/langohr "5.0.0"]
                 ]
  :uberjar-name "keychain.jar"
  :main ^:skip-aot io.hosaka.keychain
  :target-path "target/%s"
  :source-paths ["src"]
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["env/dev/resources" "resources"]
                   :env {:dev true}}})
