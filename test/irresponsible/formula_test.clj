(ns irresponsible.formula-test
  (:require [clojure.test :as t]
            [clojure.test.check :as tc]
            [clojure.test.check.clojure-test :as tt]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [irresponsible.formula :as f]))

;; generation utilities

(def some* (gen/such-that (complement nil?) gen/any))

;; basic functions

(def nonempty-string    (gen/such-that seq gen/string))

(def form-fields        (gen/map some* some*))
(def canon-fields       (gen/map some* some*))
(def error-path         (gen/vector some*))
(def errors             (gen/vector some*))
(def error-fields       (gen/map error-path errors {:min-elements 1 :max-elements 3}))
(def base-result        (gen/hash-map :form form-fields :canon canon-fields))
(def invalid-result     (gen/let [r base-result, e error-fields]
                          (assoc r :error e)))
(def dirty-valid-result (gen/fmap #(assoc % :errors []) base-result))
(def valid-result       (gen/frequency [[10 base-result] [1 dirty-valid-result]]))
(def any-result         (gen/one-of [invalid-result dirty-valid-result dirty-valid-result]))

(tt/defspec test-valid?-and-invalid?
  (prop/for-all [i invalid-result, v valid-result]
    (t/testing "valid?"
      (t/is (f/valid? v))
      (t/is (not (f/valid? i))))

    (t/testing "invalid?"
      (t/is (f/invalid? i))
      (t/is (not (f/invalid? v))))))

(tt/defspec test-truly
  (prop/for-all [x gen/any]
    (t/is (f/truly x))))

(tt/defspec test-formula?
  (prop/for-all [x (gen/such-that #(not (satisfies? f/Formula %)) gen/any)]
    (t/is (not (f/formula? x)))
    (t/is (f/formula? (reify f/Formula)))))

(tt/defspec test-deform
  (prop/for-all [x some*, y some*, z some*]
    (with-redefs [f/deform* vector]
      (and (= [x y []] (f/deform x y))
           (= [x y z]  (f/deform x y z))))))

(tt/defspec test-reform
  (prop/for-all [x some*, y some*]
    (with-redefs [f/reform* vector]
      (= [x y] (f/reform x y)))))

(defn starts? [pre all]
  (= pre (subvec all 0 (count pre))))

(defn ends? [post all]
  (= post (subvec all (- (count all) (count post)))))

(tt/defspec test-merge-results
  (prop/for-all [{fx :form ex :error dx :data :as x} any-result
                 {fy :form ey :error dy :data :as y} any-result]
    (let [{:keys [form error data] :as z} (f/merge-results x y)]
      (t/testing "original keys are retained"
        (t/is (every? #(contains? form %)  (keys fx)))
        (t/is (every? #(contains? data %) (keys dx))))
      (t/testing "secondary keys overwrite"
        (t/is (every? #(= (fy %) (form %)) (keys fy)))
        (t/is (every? #(= (dy %) (data %)) (keys dy))))
      (t/testing "error keys are merged"
        (t/is (every? #(starts? (dx %) (data %)) (keys dx)))
        (t/is (every? #(ends?   (dy %) (data %)) (keys dy)))))))

(tt/defspec test-field
  (prop/for-all [in-key  some*
                 out-key some*
                 error   some*
                 pass    gen/large-integer
                 fail    gen/keyword]
    (let [f1 (f/field {:in-key in-key :out-key out-key :error error
                       :test   integer?
                       :canon  inc
                       :deform #(+ 2 %)
                       :reform #(+ 3 %)})
          pass-res (f/deform f1 {in-key pass})
          fail-res (f/deform f1 {in-key fail})]
      (t/is (f/valid? pass-res))
      (t/is (= (+ 1 pass) (get-in pass-res [:form in-key])))
      (t/is (= (+ 2 pass) (get-in pass-res [:data out-key])))
      (t/is (f/invalid? fail-res)))))

;; (let [f1 (f/field {:in-key :foo :out-key :bar :error :baz
;;                    :test   integer?
;;                    :canon  inc
;;                    :deform #(+ 2 %)
;;                    :reform #(+ 3 %)})]
;;           (f/deform f1 {:foo :k}))

;; (field-test)

;; TODO: and* or form
