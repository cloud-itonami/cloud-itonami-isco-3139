# Contributing

Thank you for your interest in contributing to cloud-itonami-isco-3139!

## Development Setup

```bash
git clone https://github.com/cloud-itonami/cloud-itonami-isco-3139.git
cd cloud-itonami-isco-3139
clojure -M:test
```

## Code Style

- All source code is `.cljc` (portable Clojure).
- Follow standard Clojure naming conventions.
- Tests are colocated with source files and use `clojure.test`.

## Testing

All pull requests must pass the full test suite:

```bash
clojure -M:test
```

## Safety-Critical Areas

This actor enforces hard safety boundaries (no direct actuation, no forbidden operations). Changes to `governor.cljc` and the safety invariants must be reviewed carefully.

## License

All contributions are licensed under AGPL-3.0-or-later.
