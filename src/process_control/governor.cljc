(ns process-control.governor
  "ProcessControlGovernor — the independent safety/traceability layer for
  the ISCO-08 3139 process control technician operations coordination
  actor. Wired as its own `:govern` node in `process-control.actor`'s
  StateGraph, downstream of `:advise` — the Advisor has no notion of unit
  provenance or operational risk, so this MUST be a separate system able
  to reject a proposal (itonami actor pattern, per ADR-2607011000 / CLAUDE.md
  Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. unit provenance         — the request's unit-id must be registered and verified.
    2. no-actuation            — proposal :effect must be :propose.
    3. no-process-control      — proposal :op must NOT be :direct-setpoint-control,
                                 :valve-actuation, :pump-control, :alarm-suppression,
                                 or :emergency-shutdown (those are exclusively operator/engineer
                                 authority, never automated).

  ESCALATION invariants (:escalate? true, ALWAYS human sign-off):
    4. :op :flag-anomalous-reading  — always escalates (high-safety signal).
    5. low confidence (< `confidence-floor`).")
  (:require [process-control.store :as store]))

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
