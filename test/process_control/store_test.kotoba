(ns process-control.store-test
  (:require [clojure.test :refer [deftest is testing]]
            [process-control.store :as store]))

(deftest mem-store-creation
  (testing "mem-store creates an empty store"
    (let [s (store/mem-store)]
      (is (empty? (store/records s)))
      (is (empty? (store/ledger s)))))
  (testing "mem-store accepts initial units"
    (let [s (store/mem-store {:unit-1 {:name "Unit 1" :verified? true}})
          u (store/unit s :unit-1)]
      (is (= "Unit 1" (:name u))))))

(deftest commit-record
  (testing "commit-record! appends to records"
    (let [s (store/mem-store {:unit-1 {:name "Unit 1" :verified? true}})
          record {:unit-id :unit-1 :op :log-process-reading :payload {:reading 42}}]
      (store/commit-record! s record)
      (is (= 1 (count (store/records s))))
      (is (= record (first (store/records s))))))
  (testing "commit-record! requires :unit-id"
    (let [s (store/mem-store)]
      (is (thrown? #?(:clj Exception :cljs js/Error)
                   (store/commit-record! s {:op :log-process-reading}))))))

(deftest append-ledger
  (testing "append-ledger! adds entries"
    (let [s (store/mem-store)
          entry {:disposition :commit :record {:unit-id :unit-1}}]
      (store/append-ledger! s entry)
      (is (= 1 (count (store/ledger s))))
      (is (= entry (first (store/ledger s)))))))
