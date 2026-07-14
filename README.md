# cloud-itonami-isco-3139

Open Occupation Blueprint for **ISCO-08 3139**: Process Control Technicians Not Elsewhere Classified.

This repository designs a forkable OSS business for process control technician operations coordination: a document-handling and telemetry-monitoring robot performs routine operational logging, maintenance scheduling, and anomalous-condition flagging under a governor-gated actor, so a plant operator keeps its own operational and maintenance history instead of renting a closed plant-management platform.

## What This Does NOT Do

**CRITICAL SCOPE BOUNDARY:** This actor supports process control technician **back-office coordination workflow only**.

- ✗ This actor does **NOT** directly control setpoints, valve positions, pump speeds, or process parameters.
- ✗ This actor does **NOT** dispatch actuation commands, alarm suppression, or thermal/pressure control.
- ✗ This actor does **NOT** have authority over emergency shutdown, process control, or safety interlocks.

Those capabilities remain exclusively under **licensed plant operators' and engineers' human authority** and are outside this system's vocabulary. The Governor enforces this hard boundary (see `process-control.governor`).

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs the physical domain work**. Here a telemetry-monitoring robot performs routine process readings, maintenance scheduling, and anomalous-condition flagging under an actor that proposes coordination actions and an independent **Process Control Governor** that gates them. The governor never dispatches a plant operation itself; `:high`/`:safety-critical` actions (such as anomalous-reading escalation, or any proposal touching process control) require human sign-off.

## Core Contract

```text
unit registration + operational parameters + telemetry observation
        |
        v
Process Control Advisor -> Process Control Governor -> operational record, maintenance scheduling, escalation, or human approval
        |
        v
robot actions (gated) + operation records + escalation records + audit ledger
```

No automated advice can dispatch a plant operation the governor refuses, suppress an operational record, escalate a safety concern without governor approval, or propose process control without hard rejection and audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation) (ISCO-08 `3139`). Required capabilities:

- :robotics
- :identity
- :forms
- :dmn
- :bpmn
- :audit-ledger
- :telemetry

See [`docs/business-model.md`](docs/business-model.md) and [`docs/operator-guide.md`](docs/operator-guide.md).

## Reference implementation (`:maturity :implemented`)

Full itonami Actor pattern (per ADR-2607011000 / CLAUDE.md's Actors section), alongside `cloud-itonami-isco-3132`, `-isco-3131`, `-isco-2411`, `-isco-2166`, and others: a real [`kotoba-lang/langgraph`](https://github.com/kotoba-lang/langgraph) `StateGraph`, with the Advisor and Governor as distinct graph nodes and human-in-the-loop interrupt/resume via checkpointing.

```text
:intake -> :advise -> :govern -> :decide -+-> :commit            (:ok? true)
                                           +-> :request-approval   (:escalate? true, interrupt-before)
                                           +-> :hold               (:hard? true)
```

### Proposal operations (all `:effect :propose`)

- `:log-process-reading` — routine output/parameter reading logging (e.g., pressure, temperature, flow rate, concentration)
- `:schedule-maintenance` — maintenance scheduling proposal (e.g., calibration, sensor replacement, equipment inspection)
- `:flag-anomalous-reading` — surface an out-of-range reading; **ALWAYS escalates to human approval**
- `:coordinate-shift-handover` — shift-handover coordination note (handoff checklist, notable conditions)

### Hard invariants (ALWAYS `:hold`, never overridable)

1. **Unit provenance** — the request's unit-id must be registered and verified before any operation.
2. **No direct actuation** — all proposals must have `:effect :propose` only (never `:commit` or `:dispatch`).
3. **No process control** — any proposal whose `:op` is `:direct-setpoint-control`, `:valve-actuation`, `:pump-control`, `:alarm-suppression`, or `:emergency-shutdown` is permanently rejected (those are exclusively operator/engineer authority).

### Escalation invariants (ALWAYS human sign-off, per the README robotics-premise)

4. **`:flag-anomalous-reading` ALWAYS escalates** — high-safety signal, no exception.
5. **Low confidence** (< `confidence-floor` = 0.7) ALWAYS escalates — LLM parse failures or uncertain advisors.

### Implementation modules

- `src/process_control/store.cljc` — `Store` protocol + `MemStore`: registered units/systems, committed records, append-only audit ledger.
- `src/process_control/advisor.cljc` — `Advisor` protocol; `mock-advisor` (deterministic, default) proposes a process control operations coordination action from a request; `llm-advisor` wraps a `langchain.model/ChatModel` — either way the advisor only ever produces a `:propose`-effect proposal, never a committed record, and LLM parse failures always yield `confidence 0.0` (forces escalation, never fabricated confidence).
- `src/process_control/governor.cljc` — `ProcessControlGovernor/check`: a pure function, wired as its own `:govern` node. Hard invariants (unregistered/unverified unit, a proposal whose `:effect` isn't `:propose`, or process-control commands) always route to `:hold`. Escalation invariants (`:flag-anomalous-reading`, or low advisor confidence) always route to `:request-approval` — an `interrupt-before` node that the graph checkpoints and only resumes on explicit human approval (`actor/approve!`).
- `src/process_control/actor.cljc` — `build-graph`, `run-request!`, `approve!`: the `langgraph.graph/state-graph` wiring itself.

```bash
clojure -M:test
```

This is what backs this repo's `:maturity :implemented` entry in [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation).

## License

AGPL-3.0-or-later.
