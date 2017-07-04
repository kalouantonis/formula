(ns irresponsible.confrom-test
  (:require  [clojure.test :as t]
             [clojure.spec.alpha :as s]
             [irresponsible.formula.conform :as c]))

(t/deftest invalid?-test
  (t/is (c/invalid? ::s/invalid))
  (doseq [i [1 1.2 \a "a" :a 'a [] {} ()]]
    (t/testing i
      (t/is (not (c/invalid? i))))))

(t/deftest try-conform-test
  (t/is (c/invalid? (c/try-conform (throw (ex-info "" {})))))
  (t/is (= ::sentinel  (c/try-conform ::sentinel))))

(t/deftest keep-conformer-test
  (t/is (c/invalid? ((c/keep-conformer (constantly nil)) 123)))
  (t/is (= 123 ((c/keep-conformer identity) 123))))

(t/deftest pred-conformer-test
  (let [t1 (c/pred-conformer integer?)
        t2 (c/pred-conformer integer? #(* 2 %))]
    (doseq [i ["" [] {} () :a 'a 1.23]]
      (t/is (c/invalid? (t1 i))))
    (t/is (= 2 (t1 2)))
    (t/is (= 4 (t2 2)))))

(t/deftest conp-test
  (let [t1 (c/conp (c/min 10) c/parse-long)]
    (t/is (= 123 (t1 "123")))
    (t/is (c/invalid? (t1 "abc")))
    (t/is (c/invalid? (t1 "09")))))
