# Governance: Process Control Actor

## Architecture

This actor is built on the **itonami Actor pattern** (see ADR-2607011000 in the kotoba-lang monorepo):

```
Advisor (proposes) -> Governor (gates) -> StateGraph (decides/commits) -> Audit Ledger (records)
```

## The Governor's Authority

The `ProcessControlGovernor` is the sole gatekeeper for all operations. It enforces:

1. **Hard Invariants** (ALWAYS `:hold`, never overridable)
   - Unit must be registered and verified
   - Proposals must have `:effect :propose` only
   - Forbidden operations (direct setpoint, valve actuation, pump control, alarm suppression, emergency shutdown) are permanently blocked

2. **Escalation Invariants** (ALWAYS human sign-off)
   - `:flag-anomalous-reading` always escalates
   - Low-confidence proposals (< 0.7) always escalate

## Checkpointing & Resume

The StateGraph checkpoints before each escalation node (`:request-approval`). A technician can:

1. Review the proposal and escalation reason
2. Approve via `actor/approve!` to advance to commit
3. Reject by not resuming (the checkpoint persists for audit)

## Audit Trail

All decisions are appended to an immutable ledger:

```clojure
{:disposition :commit|:hold|:escalate
 :record {...}      ; for commit
 :verdict {...}}    ; for hold/escalate
```

This ensures full traceability: what the advisor proposed, what the governor decided, and why.

## No Backdoor Actuation

The actor never directly dispatches process control commands. It only proposes and records. A technician remains in control.
