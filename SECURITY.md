# Security & Safety

## Critical Boundaries

This actor **never** directly controls process operations. The following are **hard boundaries that cannot be overridden**:

- **No setpoint control** — technician authority only
- **No valve actuation** — technician authority only
- **No pump control** — technician authority only
- **No alarm suppression** — technician authority only
- **No emergency shutdown** — technician authority only

The Governor enforces these as irreversible hard violations. A proposal attempting any of these will be rejected and audit-logged.

## Escalation as a Safety Feature

The actor escalates:
- Any anomalous-reading flag (high-safety signal)
- Any proposal with confidence < 0.7 (LLM parse failures, uncertainty)

These escalations require explicit human approval before proceeding.

## Audit Logging

Every proposal, decision, and rejection is recorded in an immutable ledger. This ensures:
- Full traceability of all actor recommendations
- Proof of human oversight and approval
- Compliance with operational and regulatory requirements

## Reporting Security Issues

If you discover a security vulnerability, please email `jun784@gmail.com` with details. Do not open a public issue.

## Supply Chain

Dependencies are pinned to specific git SHAs in `deps.edn`. Before using, verify:
- `io.github.kotoba-lang/langgraph` SHA matches the expected commit
- No unexpected transitive dependencies are introduced

## Compliance

This actor is designed to support human-in-the-loop industrial safety workflows. It is not a replacement for:
- Licensed plant operators
- Professional safety engineering
- Regulatory compliance systems
- Emergency response procedures
