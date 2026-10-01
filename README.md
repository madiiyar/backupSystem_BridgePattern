# Backup System — Bridge + Adapter Pattern (Assignment 3)

A backup system demonstrating the **Bridge** and **Adapter** design patterns
working together, with a dynamic-selection complexity module.

## Problem

Two things vary independently:
- **What kind of backup** runs (Full, Incremental)
- **Where it's stored** (local disk, in-memory, legacy tape archive)

Bridge separates these into two independent hierarchies connected by
composition, avoiding a combinatorial explosion of subclasses. One storage
backend (`TapeArchiveDriver`) is a legacy class with an incompatible
interface (different method names, swapped parameter order, int error
codes instead of exceptions) — `TapeStorageAdapter` wraps it using the
Adapter pattern so it can slot into the Bridge's Implementor role without
any changes to its source.

## Package structure

\`\`\`
backup/
├── bridge/     Abstraction side: BackupJob, FullBackup, IncrementalBackup, FileEntry
├── storage/    Implementor interface (StorageTarget) + native backends
│   └── exceptions/   StorageException hierarchy (4 types)
├── legacy/     TapeArchiveDriver — untouched legacy class (the Adaptee)
├── adapter/    TapeStorageAdapter — wraps TapeArchiveDriver into StorageTarget
└── app/        StorageTargetResolver (dynamic selection), BackupService, Main
\`\`\`

## Complexity module

**Dynamic implementor selection.** `StorageTargetResolver` picks the
concrete `StorageTarget` at runtime from a URI scheme (`file://`,
`mem://`, `tape://`) — the client never hard-codes which backend is used.

## Build & run

Requires JDK 17+ and Maven.

\`\`\`bash
mvn test       # run the unit test suite
mvn package    # build the jar
# run backup.app.Main directly from your IDE
\`\`\`

## Tests

11 JUnit 5 tests (Mockito for interaction verification):
- `FullBackupTest`, `IncrementalBackupTest` — delegation logic (mocked `StorageTarget`)
- `TapeStorageAdapterTest` — failure-code translation + upload/exists round-trip (real `TapeArchiveDriver`)
- `StorageTargetResolverTest` — scheme-based dynamic dispatch

## Documents

- [`docs/uml-diagram.png`](docs/uml-diagram.png) — class diagram
- [`docs/design-rationale.md`](docs/design-rationale.md) — design rationale (problem, why Bridge/Adapter alone wouldn't suffice, incompatibility justification, limitation)