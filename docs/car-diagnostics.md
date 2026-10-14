# Android Auto diagnostic logs

The phone's Android Auto tab has a separate scrolling journal showing the last 100
persisted events. Android Auto data and buttons stay fixed; the "Diagnostic logs"
heading, storage errors and entries scroll together in one LazyColumn. Bluetooth
code and its history are separate.

The phone tab requests `com.google.android.gms.permission.CAR_MILEAGE` at runtime.
After denial, use **App settings** to grant it. Returning to the app rechecks access.
The car screen also offers **Allow mileage access**, using a parked-only permission
request. The mileage source checks permission before subscribing and resumes after
a grant; it does not register a listener without permission or after the session stops.

Set **Car ID** before connecting, and change it when using another car. IDs are
manual, case-sensitive labels; Android Auto does not supply a unique vehicle ID
through the APIs used by this app. Reconnecting with the same ID within 120 seconds
of session destruction continues the previous log session. A longer gap, a different
ID, or an unidentified car starts a new session. Merely hiding the car screen does
not disconnect the car. Naming an unidentified live car preserves its existing
session; changing an already assigned ID starts a new session immediately.

- **Export all logs** writes the entire history to `car_diagnostics.jsonl`. Select a
  phone folder the first time; subsequent exports reuse the granted folder.
- **Export last session** asks for a name and writes every record from the latest
  session, including its reconnects, not just the 100 visible records.
- An existing destination always offers **Append**, **Replace**, and **Change name**.
  Append adds the selected snapshot again; it does not deduplicate previous exports.
  Exported files remain accessible through the phone's Files app in the selected folder.

## Structure and persistence

`CarDiagnosticsApplication` initializes the process-wide `DiagnosticRepository`,
including when the car service starts without the phone Activity. The repository
serializes callbacks, car-ID changes, disk writes and export barriers through one IO
consumer. Its lifetime is independent of the phone screen and car session.

`CarDiagnosticStore` appends UTF-8 JSONL to `filesDir/car_diagnostics.jsonl`. It never
rotates away old history. Each record contains `timestampMillis`, `message`,
`sessionId`, `carId`, and a `lastSession` checkpoint. The checkpoint preserves the
latest session's ID, starting byte offset and disconnect time across process restarts.
The selected car ID is stored separately in app preferences. Phone-only events outside
a car connection have a null `sessionId` and are included only in the full export.

Startup scans backwards in blocks to restore the last 100 lines, discarding an
unfinished final line. Complete malformed records are skipped when restoring the UI
or filtering a session. After an unexpected process death, the last persisted event
is used as the conservative disconnect boundary.

Exports stream a fixed committed byte range; subsequent appends do not change that
snapshot. Session exports start at the saved session offset and filter by session ID.
The SAF document provider handles shared files; no broad storage permission is needed.
Append uses `wa`, replacement uses `wt`, and neither operation runs on the main thread.

Disk failures are displayed and retried without dropping the failed queued event.
As with any asynchronous logger, events still queued when the process is killed are
not durable. The queue can grow during a sustained IO failure, and the full history
uses increasing storage. Files are closed after each append; power-loss durability
is not guaranteed by an fsync on every event. A provider failure during replacement
or append can leave a partially written exported file; the internal journal remains
unchanged and can be exported again.

## Verification

JVM tests cover concurrent appends, tail recovery, bounded snapshots, filename rules,
session timeouts, car changes, stale disconnect callbacks and restart recovery.
`CarLogExportInstrumentedTest` uses an isolated document provider and temporary cache
files to check JSONL encoding, create/append/replace, collision protection, and session
filtering. It does not write into the user's shared folders.
