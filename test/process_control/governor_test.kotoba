(ns process-control.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [process-control.governor :as gov]
            [process-control.store :as store]))

(deftest hard-violations-unregistered-unit
  (testing "unregistered unit triggers hard violation"
    (let [proposal {:op :log-process-reading :effect :propose}
          request {:unit-id :nonexistent}
          s (store/mem-store {})
          verdict (gov/check request nil proposal s)]
      (is (:hard? verdict))
      (is (not (:ok? verdict)))
      (is (seq (:violations verdict))))))

(deftest hard-violations-unverified-unit
  (testing "unverified unit triggers hard violation"
    (let [proposal {:op :log-process-reading :effect :propose}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? false}})
          verdict (gov/check request nil proposal s)]
      (is (:hard? verdict))
      (is (not (:ok? verdict))))))

(deftest hard-violations-non-propose-effect
  (testing "non-:propose effect triggers hard violation"
    (let [proposal {:op :log-process-reading :effect :commit}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? true}})
          verdict (gov/check request nil proposal s)]
      (is (:hard? verdict))
      (is (not (:ok? verdict))))))

(deftest hard-violations-forbidden-ops
  (testing "direct setpoint control is forbidden"
    (let [proposal {:op :direct-setpoint-control :effect :propose}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? true}})
          verdict (gov/check request nil proposal s)]
      (is (:hard? verdict))))
  (testing "valve actuation is forbidden"
    (let [proposal {:op :valve-actuation :effect :propose}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? true}})
          verdict (gov/check request nil proposal s)]
      (is (:hard? verdict))))
  (testing "pump control is forbidden"
    (let [proposal {:op :pump-control :effect :propose}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? true}})
          verdict (gov/check request nil proposal s)]
      (is (:hard? verdict))))
  (testing "alarm suppression is forbidden"
    (let [proposal {:op :alarm-suppression :effect :propose}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? true}})
          verdict (gov/check request nil proposal s)]
      (is (:hard? verdict))))
  (testing "emergency shutdown is forbidden"
    (let [proposal {:op :emergency-shutdown :effect :propose}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? true}})
          verdict (gov/check request nil proposal s)]
      (is (:hard? verdict)))))

(deftest escalation-anomalous-reading
  (testing "flag-anomalous-reading always escalates"
    (let [proposal {:op :flag-anomalous-reading :effect :propose :confidence 0.95}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? true}})
          verdict (gov/check request nil proposal s)]
      (is (not (:hard? verdict)))
      (is (:escalate? verdict))
      (is (not (:ok? verdict))))))

(deftest escalation-low-confidence
  (testing "low confidence triggers escalation"
    (let [proposal {:op :log-process-reading :effect :propose :confidence 0.5}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? true}})
          verdict (gov/check request nil proposal s)]
      (is (not (:hard? verdict)))
      (is (:escalate? verdict))
      (is (not (:ok? verdict))))))

(deftest ok-proposal
  (testing "valid proposal with high confidence is ok"
    (let [proposal {:op :log-process-reading :effect :propose :confidence 0.85}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? true}})
          verdict (gov/check request nil proposal s)]
      (is (not (:hard? verdict)))
      (is (not (:escalate? verdict)))
      (is (:ok? verdict)))))

(deftest ok-schedule-maintenance
  (testing "schedule-maintenance proposal is ok with high confidence"
    (let [proposal {:op :schedule-maintenance :effect :propose :confidence 0.8}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? true}})
          verdict (gov/check request nil proposal s)]
      (is (:ok? verdict)))))

(deftest ok-shift-handover
  (testing "shift-handover proposal is ok with high confidence"
    (let [proposal {:op :coordinate-shift-handover :effect :propose :confidence 0.9}
          request {:unit-id :unit-1}
          s (store/mem-store {:unit-1 {:name "Unit 1" :status :operational :verified? true}})
          verdict (gov/check request nil proposal s)]
      (is (:ok? verdict)))))
