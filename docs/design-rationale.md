# Design Rationale — Backup System (Bridge + Adapter)

## 1. Problem domain

The system backs up files to a destination. Two aspects of this problem
vary independently:

- **What kind of backup runs** — a *Full* backup uploads every file; an
  *Incremental* backup uploads only files that don't already exist at the
  destination.
- **Where the backup goes** — local disk, an in-memory store, or a legacy
  tape archive system (`TapeArchiveDriver`) that already exists in the
  organization and cannot be modified.

Any backup type must be able to run against any destination: a full
backup to tape, an incremental backup to disk, and so on, without writing
a separate class for each combination.

## 2. Why Bridge alone would not be enough

Without Bridge, supporting *N* backup types against *M* destinations by
inheritance alone requires up to *N × M* classes (`FullBackupToDisk`,
`FullBackupToTape`, `IncrementalBackupToDisk`, ...). Every new backup
type or new destination multiplies the number of classes needed. Bridge
splits the problem into two independent hierarchies — `BackupJob`
(Abstraction: `FullBackup`, `IncrementalBackup`) and `StorageTarget`
(Implementor: `LocalDiskTarget`, `InMemoryTarget`, `TapeStorageAdapter`)
— connected by composition rather than inheritance. This project has
2 backup types and 3 destinations, covering all 6 combinations with only
5 classes, and adding a new type or destination never requires touching
the other hierarchy.

But Bridge alone is not sufficient, because one of the three required
destinations is a pre-existing class, `TapeArchiveDriver`, whose
interface does not match `StorageTarget` at all. Bridge assumes every
Implementor can be written to satisfy the Implementor interface
directly; it has no mechanism for reconciling a mismatched interface.
Without something to bridge that gap, `TapeArchiveDriver` simply cannot
occupy the Implementor slot.

## 3. Why Adapter alone would not be enough

Adapter alone solves the interface-mismatch problem for `TapeArchiveDriver`,
but by itself it says nothing about how backup *policy* (full vs.
incremental) should relate to storage *destinations*. Without Bridge's
separation of these two concerns, the adapted tape class would still
need to be paired with backup-type logic through inheritance or
duplicated code, reintroducing the combinatorial problem Bridge exists
to avoid. Adapter solves "how do I make this one incompatible class fit
the contract"; Bridge solves "how do I let backup type and destination
vary independently." The project needs both, solving two different
problems that happen to meet at one interface (`StorageTarget`).

## 4. Why `TapeArchiveDriver` is genuinely incompatible

`TapeArchiveDriver` differs from `StorageTarget` in three independent
ways, not just a naming difference:

1. **Different method names and signatures** — `put`/`stat`/`erase`
   instead of `upload`/`exists`/`delete`.
2. **Different parameter order and types** — `put(tapeLabel,
   localFilePath)` takes the label before the data, and expects a local
   file path (`String`) rather than raw bytes (`byte[]`); `StorageTarget`
   expects `upload(path, data)`.
3. **Different failure mechanism** — `TapeArchiveDriver` reports failure
   as `int` status codes (`0`, `-1`, `-3`, `-7`) and never throws; every
   other `StorageTarget` implementation reports failure via the checked
   `StorageException` hierarchy.

`TapeStorageAdapter` resolves all three: it stages incoming bytes to a
temporary file before calling `put()`, reorders and reshapes the call,
and translates every returned status code into the matching
`StorageException` subtype (`StorageFullException`,
`StorageUnavailableException`, `StorageIOException`) inside a single
`translate()` method. No code outside `TapeStorageAdapter` ever sees a
tape-specific status code or the `TapeArchiveDriver` type — the
`BackupJob` hierarchy depends only on `StorageTarget`.

## 5. Complexity module: dynamic implementor selection

This project uses **dynamic implementor selection**. `StorageTargetResolver`
holds a registry of factories keyed by URI scheme (`file`, `mem`, `tape`)
and builds the matching `StorageTarget` — including the adapted tape
implementation — at runtime, from a destination string supplied by the
caller (e.g. `"tape://vault-01"`). No class in the system hard-codes
which concrete implementor is used; adding a new backend means
registering one new factory entry, not modifying `BackupJob`,
`StorageTarget`, or any existing implementor.

## 6. Limitation

`StorageTargetResolver` constructs a brand-new `StorageTarget` instance
(and, for the tape scheme, a brand-new `TapeArchiveDriver`) on every call
to `resolve()`, rather than caching and reusing instances per
destination. This means two successive `IncrementalBackup` runs against
the same tape URI do not see each other's state — the tape driver starts
empty each time, so the second run uploads files the first run already
wrote, rather than skipping them. A production version would need a
target registry keyed by destination URI so that state persists across
calls to the same destination.