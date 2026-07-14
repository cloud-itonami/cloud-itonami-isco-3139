# Operator Guide: Process Control Technician Actor

## Quick Start

The Process Control Actor is a `langgraph.graph/state-graph` that coordinates operational proposals for process control systems. It does **not** control the process itself — it coordinates the technician's workflow.

## Request Format

A process control request:

```clojure
{:unit-id :some-unit-id
 :op :log-process-reading          ; or :schedule-maintenance, :flag-anomalous-reading, :coordinate-shift-handover
 :payload {...}}
```

## Running the Actor

```clojure
(require '[process-control.actor :as actor]
         '[process-control.store :as store])

(def s (store/mem-store {:unit-1 {:name "Reactor 1" :verified? true}}))
(def g (actor/build-graph {:store s}))

; Run a request
(def result (actor/run-request! g {:unit-id :unit-1 :op :log-process-reading :payload {:pressure 50 :temp 120}} nil "thread-1"))

; If it escalates (:status :interrupted), approve it:
(actor/approve! g "thread-1")
```

## What the Governor Does

The governor checks every proposal:

1. **Unit must be registered and verified** — no orphaned requests.
2. **Effect must be `:propose`** — no automation bypassing the approval layer.
3. **Forbidden operations are rejected** — no setpoint control, valve actuation, pump control, alarm suppression, or emergency shutdown.
4. **Anomalies always escalate** — no suppression.
5. **Low confidence always escalates** — LLM parse failures or uncertain advisors require human review.

## Audit Trail

Every decision is logged to the immutable ledger:

```clojure
(store/ledger s)
=> [{:disposition :commit :record {...}}
    {:disposition :hold :verdict {...}}
    ...]
```

## Safety Boundaries

This actor coordinates workflow, not control. A technician is still responsible for:
- Direct emergency shutdown
- Setpoint adjustments
- Alarm suppression (if ever needed)
- Manual overrides

The actor proposes, the technician decides. The governor ensures no proposal can bypass this.
