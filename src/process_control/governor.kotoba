(ns process-control.governor
  (:require [process-control.store :as store]))

; Governor checks every proposal
; Hard invariants: always :hold, never overridable
; - unit must be registered and verified
; - effect must be :propose only (no actuation)
; - forbidden operations are permanently rejected
; Escalation invariants: always human sign-off
; - flag-anomalous-reading always escalates
; - low confidence (< 0.7) always escalates

(def confidence-floor 0.7)
(def ^:private forbidden-ops
  #{:direct-setpoint-control :valve-actuation :pump-control
    :alarm-suppression :emergency-shutdown})
(def ^:private escalating-ops #{:flag-anomalous-reading})

(defn- hard-violations [{:keys [proposal]} unit-record]
  (cond-> []
    (nil? unit-record)
    (conj {:rule :no-unit :detail "unregistered or unverified unit"})

    (not (:verified? unit-record))
    (conj {:rule :unverified-unit :detail "unit must be verified before operations"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect must be :propose only (no direct actuation)"})

    (contains? forbidden-ops (:op proposal))
    (conj {:rule :no-process-control :detail "direct setpoint control, valve actuation, pump control, alarm suppression, and emergency shutdown are exclusively operator/engineer authority"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `process-control.store/Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [unit-record (store/unit store (:unit-id request))
        hard (hard-violations {:proposal proposal} unit-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        risky-op? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not risky-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? risky-op?))}))
