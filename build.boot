; vim: syntax=clojure
(set-env!
  :project 'irresponsible/formula
  :version "0.1.0"
  :resource-paths #{"src" "resources"}
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.9.0-alpha17"]
                  [org.clojure/core.match "0.3.0-alpha4"]
                  [org.clojure/math.numeric-tower "0.0.4"]
                  [org.clojure/test.check "0.10.0-alpha2"]
                  [org.flatland/ordered "1.5.5"]
                  [commons-validator/commons-validator "1.6"]
                  [adzerk/boot-test "1.2.0"  :scope "test"]])

(require '[adzerk.boot-test :as t])

(task-options!
  pom  {:project (get-env :project)
        :version (get-env :version)
        :description "Webforms, done with spec"
        :url "https://github.com/irresponsible/formula"
        :scm {:url "https://github.com/irresponsible/formula"}
        :license {"MIT" "https://en.wikipedia.org/MIT_License"}}
  push {:tag true
        :ensure-branch "master"
        :ensure-release true
        :ensure-clean true
        :gpg-sign true
        :repo "clojars"}
  target {:dir #{"target"}})

(deftask testing []
  (set-env! :source-paths  #(conj % "test")
            :resource-paths #(conj % "test"))
  identity)

(deftask test []
  (comp (testing) (t/test)))
