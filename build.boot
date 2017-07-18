; vim: syntax=clojure
(set-env! :dependencies
  (cond (= "1.8.0" (System/getenv "BOOT_CLOJURE_VERSION"))
        '[[org.clojure/clojure "1.8.0" :scope "provided"]]
        :else
        '[[org.clojure/clojure "1.9.0-alpha17" :scope "provided"]]))

(set-env!
  :project 'irresponsible/formula
  :version "0.1.0"
  :resource-paths #{"src" "resources"}
  :source-paths #{"src"}
  :dependencies #(into % '[[adzerk/boot-test       "1.2.0"         :scope "test"]
                           [org.clojure/test.check "0.10.0-alpha2" :scope "test"]]))

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
