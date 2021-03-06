(ns datomish.util-test
  #?(:cljs
     (:require-macros
      [cljs.core.async.macros :as a :refer [go go-loop]]))
  (:require
   [datomish.util :as util]
   #?@(:clj [[clojure.test :as t :refer [is are deftest testing]]
             [clojure.core.async :as a :refer [go go-loop <! >!]]])

   #?@(:cljs [[cljs.test :as t :refer-macros [is are deftest testing]]
              [cljs.core.async :as a :refer [<! >!]]])))

(deftest test-var-translation
  (is (= :x (util/var->sql-var '?x)))
  (is (= :XX (util/var->sql-var '?XX))))

#?(:cljs
   (deftest test-integer?-js
     (is (integer? 0))
     (is (integer? 5))
     (is (integer? 50000000000))
     (is (integer? 5.00))             ; Because JS.
     (is (not (integer? 5.1)))))

#?(:clj
   (deftest test-integer?-clj
     (is (integer? 0))
     (is (integer? 5))
     (is (integer? 50000000000))
     (is (not (integer? 5.00)))
     (is (not (integer? 5.1)))))

#?(:cljs
   (deftest test-raise
     (let [caught
           (try
             (do
               (util/raise "succeed" {:foo 1})
               "fail")
             (catch :default e e))]
       (is (= "succeed" (aget caught "message")))
       (is (= {:foo 1} (aget caught "data"))))))

(deftest test-unblocking-chan?
  (is (util/unblocking-chan? (a/chan (a/dropping-buffer 10))))
  (is (util/unblocking-chan? (a/chan (a/sliding-buffer 10))))
  (is (util/unblocking-chan? (a/chan (util/unlimited-buffer))))
  (is (not (util/unblocking-chan? (a/chan (a/buffer 10))))))

(deftest test-group-by-kvs
  (are [m xs] (= m (util/group-by-kv identity xs))
    {:a [1 2] :b [3]}
    [[:a 1] [:a 2] [:b 3]]))

(deftest test-repeated-keys
  (let [abc {:a 1 :b 2 :c 3}
        def {:d 1 :e 2 :f 3}
        bcd {:b 1 :c 2 :d 3}
        efg {:e 1 :f 2 :g 3}
        empty {}]
    (is (= #{} (util/repeated-keys [])))
    (is (= #{} (util/repeated-keys [empty])))
    (is (= #{} (util/repeated-keys [empty empty])))
    (is (= #{} (util/repeated-keys [abc empty empty])))
    (is (= #{} (util/repeated-keys [abc def empty])))
    (is (= #{:b :c} (util/repeated-keys [bcd abc])))
    (is (= #{:b :c :d} (util/repeated-keys [abc def bcd])))
    (is (= #{:b :c :d :e :f :g} (util/repeated-keys [abc efg def efg bcd])))))
