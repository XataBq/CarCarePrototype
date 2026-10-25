# Android Auto mileage reliability

Implementation based on `origin/master` at `0f15f30`, checked on 2026-10-25.
Car App Library remains at 1.7.0. No Bluetooth, maintenance calculation,
application ID, signing, Play configuration or export format changes are needed.

## Findings and boundaries

The previous `repeatOnLifecycle(STARTED)` was already a valid mechanism for
resubscribing. It collects the permission StateFlow's initial value on each entry
to STARTED. It is retained. The previous implementation had these app-side issues:

- `SUBSCRIBING` and unsuccessful callbacks replaced the last successful reading
  with null; the UI conflated request state, response and measurement.
- A single listener instance was reused. An old queued callback could pass the
  `active` check after Stop/Start and be treated as a new response.
- There was no diagnostic deadline or manual retry.
- Displayed mileage was not cleared on vehicle-label changes or new connections.
- Listener ownership was discarded even if removal failed, potentially allowing
  a subsequent registration over an unsuccessfully removed listener.
- Logs lacked attempt identity, receipt clocks and callback latency.

The following are host-side observations, not fixed or proven vehicle behavior:

- Kia's UNKNOWN/null/0 is an actual response, not an absent callback.
- Tiguan's 45.2 km is a successful API value, not proof of a total odometer.
  A trip-counter interpretation remains unconfirmed.
- A successful registration cannot force the head unit to deliver another event.
  Returning to the app and Retry may still time out. Neither triggers endless
  automatic registrations or reconnects.

## State, lifecycle and ownership

`DiagnosticState.mileage` now contains the current request status, the last raw
reply, and the last valid numeric reading with local receipt wall time. A SUCCESS
requires a finite, non-negative value; zero is allowed. SUCCESS/null/NaN/negative
values remain visible as raw responses but are not promoted to a valid reading.

All readings are `UNVERIFIED`. `RECEIVED_THIS_ATTEMPT (sensor age unverified)`
means that a valid callback was received during the active attempt; it does not
assert sensor freshness or a total odometer. `PREVIOUS_READING` identifies a
retained reading after Stop, retry, errors or an invalid reply. Receipt date/time
is displayed, independent of the host timestamp. No mileage is used for service
interval calculations or added to any vehicle total.

Session STARTED -> permission check -> unique attempt ID -> registration ->
one 15-second deadline. Session STOP -> invalidate attempt -> cancel deadline ->
remove listener. DESTROY repeats cleanup safely and drops the retry route.
Permission StateFlow collection resumes on each STARTED entry. Each actual
registration creates a fresh listener; stale callbacks and timers check attempt
identity. All CarContext/CarInfo operations and timeout callbacks run on main.

Retry is available on the phone and as a parked-only Pane action. It removes the
existing listener before registration. Repeated clicks while SUBSCRIBING are
coalesced into the current attempt. After a response or timeout, Retry creates a
new attempt. A failed listener removal prevents new registration until cleanup
succeeds. A stopped session rejects Retry and explains that the car app must be
opened. Queued phone commands cannot target a later STARTED interval.

`NO_CALLBACK_TIMEOUT` means there was no callback. `NO_VALID_DATA_TIMEOUT` means
there was a callback but no valid number. The last raw response (e.g. UNKNOWN)
remains independently accessible. Timeout leaves the listener registered; a late
valid response updates the reading and changes the request state to SUCCESS.

## Vehicle isolation and persistence

Manual Car ID is only a log label. There is no trusted vehicle identity in this
implementation, so a new physical connection always clears the displayed reading,
even when the manual label is unchanged. Leaving and returning to the app within
the same connection retains the reading as previous until a valid new callback.
Changing or re-entering a label clears the reading and renews an active attempt.
Unidentified cars obey the same connection boundary.

The IO actor owns `MileageStateScope`. Connection tokens reject old connections;
a selection revision rejects queued updates from before a label change. The main
thread revision signal invalidates callbacks before their collector resumes.
`CarSessionTracker` and its two-minute grouping remain unchanged: grouping logs
does not justify carrying mileage between physical connections.

The existing JSONL schema is unchanged. New metadata is in the `message` string;
existing Kia/Tiguan logs, tail loading, session filtering and exports retain their
format. The full history is retained. Live measurement state is intentionally not
restored from journal history after process restart because vehicle identity is
not established. Previous numeric readings remain available in historical logs.

## Timestamp contract

The [CarValue contract](https://developer.android.com/reference/androidx/car/app/hardware/common/CarValue)
specifies milliseconds on the `SystemClock.elapsedRealtime()` time base and
monotonically increasing timestamps for a property. A value such as
`877712202363415` is suspicious but does not establish a different unit.

Each accepted callback logs the unmodified timestamp, raw value/status, interpreted
status, local receipt wall time, elapsedRealtime milliseconds/nanoseconds,
registration-to-callback latency, and comparison flags for timestamps ahead of
local elapsed milliseconds or lower than the previous reply. These are diagnostic
comparisons, not conversions. No host timestamp determines UI receipt time or
deadline scheduling. Clock problems do not silently rewrite the numeric reading.

Despite the 1.7 accessor name `odometerMeters`, values remain kilometers, consistent
with the [Mileage documentation](https://developer.android.com/reference/androidx/car/app/hardware/info/Mileage).
The correctly named accessor was added in 1.8; upgrading is not necessary here.
The [Session lifecycle contract](https://developer.android.com/reference/androidx/car/app/Session)
requires START listeners to be removed on STOP; the subscription follows it.

## Verification

Run from this worktree with Android SDK configured in local.properties:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:GRADLE_USER_HOME = 'C:\Users\XataB\.gradle'
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --offline --console=plain
```

Unit tests use an injected backend, clock and cancellable scheduler, without sleeps
or a live car host. They cover initial success, all supported raw statuses,
missing/invalid/late callbacks, timeout boundaries, Stop/Start, rapid switching,
repeated Retry, old callbacks and timers, retained readings, vehicle-scope changes,
permission denial/grant/revocation, partial registration and failed removal,
timestamp anomalies and car-screen projection. Existing store/session tests run
alongside them. These tests do not substitute for Android host integration.

Final local result: `BUILD SUCCESSFUL`; 76 JVM tests, zero failures/errors;
debug APK assembled. Lint completed with zero errors and 21 warnings (dependency
versions, manifest/resource configuration and KTX suggestions). No instrumented
tests were run. The unit and lint HTML reports are under `app/build/reports/`.

DHU is installed in this environment, but `adb devices -l` returned no attached
devices on 2026-10-25. No live DHU, phone installation or physical-car verification
was performed for this implementation. The successful DHU run on 2026-10-14 was
for the earlier implementation and is not evidence for these changes.

## Manual DHU procedure

1. Connect and authorize a phone, enable Android Auto developer mode and start
   its head unit server. Install `app/build/outputs/apk/debug/app-debug.apk` using
   `adb install -r` only when its signature matches the existing installation;
   preserve the existing diagnostic logs.
2. Grant CAR_MILEAGE via the phone app. Save this as `dhu-mileage.ini`:

   ```ini
   [general]
   resolution = 1280x720
   dpi = 160
   touch = true
   [sensors]
   odometer = true
   driving_status = true
   parking_brake = true
   gear = true
   ```

3. Run `adb forward tcp:5277 tcp:5277`, then:

   ```powershell
   & "$env:LOCALAPPDATA\Android\Sdk\extras\google\auto\desktop-head-unit.exe" --config=dhu-mileage.ini --adb=127.0.0.1:5277
   ```

4. In the DHU console use `restrict none` and `parking_brake true`. Open this app.
   Send `odometer 45.2`. Expect SUCCESS, approximately 45.2 km, UNVERIFIED, and a
   local receipt time. Export and inspect the corresponding callback metadata.
5. Open another Android Auto app, then return. Expect STOP/removal and
   START/new-attempt records. If no new callback arrives, retain the previous
   reading with PREVIOUS_READING and show a diagnostic timeout after 15 seconds.
   Some hosts replay data automatically; that is also valid.
6. Send `odometer 45.3` after the deadline. Expect SUCCESS without USB reconnect.
   Press Retry several times quickly: at most one active listener, with a new
   attempt followed by coalesced clicks while waiting. Send `odometer 45.4`.
7. Change the phone's car label. Confirm that the old reading disappears and old
   attempts cannot repopulate it. Disconnect/reconnect with the same label and
   repeat with an unidentified car: no displayed reading crosses connections.
8. Deny/revoke permission and retry; expect no registration without permission.
   Grant permission, open/return to the car app and send another odometer value.
9. Export all logs and the last-session logs. Check that older records still load
   and that export append/replace behavior remains available.
10. Exit DHU and remove only the forwarding created here:
    `adb forward --remove tcp:5277`.

DHU does not guarantee reproduction of every vehicle status or missing-callback
pattern. UNKNOWN/UNIMPLEMENTED/UNAVAILABLE and silent-host paths are covered by
unit tests; do not claim them as live DHU results unless observed. Refer to the
[official DHU guide](https://developer.android.com/training/cars/testing/dhu).

## Tiguan 2019 retest

1. Park the vehicle. Record total odometer and trip counter separately, phone and
   Android Auto versions, head-unit software, time, and the manually assigned ID.
2. Connect by USB, grant permission and open CarCarePrototype. Wait at least 15
   seconds. Record request status, last API status/value/raw timestamp, receipt
   time and UNVERIFIED marker. Do not treat 45.2 km as a confirmed trip counter.
3. Switch to another Android Auto app and return three times. Check that each
   STOP removes its listener and each START creates one new attempt. Previous
   reading must stay visible with its original receipt time until a valid reply.
4. If no data arrives, record which timeout occurred and the preserved raw status.
   Press Retry while parked, wait at least 15 seconds, and then test several rapid
   clicks. No USB reconnect should be needed to perform the retry itself; receiving
   data still depends on the host.
5. Compare any new value with both dashboard counters, without adjusting units or
   adding it to the total odometer. If it remains 45.2, retain it exactly as an
   unverified host reading. Do not reset vehicle counters as part of this test.
6. Change Car ID and confirm clearance, then reconnect USB with the original ID.
   Logs may share their two-minute export session, but the displayed reading must
   start empty until a new valid callback.
7. Export last-session JSONL with a descriptive name. Compare connection/attempt
   IDs, START/STOP events, permission checks, registration and callback times,
   timeouts, raw timestamp comparisons and removal errors. Preserve original
   Kia/Tiguan captures for comparison.

## Changed files

- `car/CarConnectionService.kt`: service creation diagnostics.
- `car/CarSession.kt`: lifecycle diagnostics, scoped retry route and identity observer.
- `car/CarMileagePermission.kt`: propagate explicit grant/denial results.
- `car/MileageDataSource.kt`: Android main-thread backend and permission adapter.
- `car/MileageSubscription.kt`: attempt ownership, cleanup, retries and deadlines.
- `car/MileageState.kt`: independent request/reply/reading state.
- `car/MileageStateScope.kt`: IO-actor identity gate for queued updates.
- `car/MileageRetry.kt`: lifecycle-scoped phone command and feedback.
- `car/CarScreen.kt`, `car/CarScreenState.kt`: parked retry and diagnostic display.
- `diagnostics/DiagnosticRepository.kt`: scoped state updates using existing actor/journal.
- `presentation/state/DiagnosticState.kt`, `presentation/DiagnosticScreen.kt`:
  shared mileage model and phone controls; logs keep their separate LazyColumn.
- `CarScreenStateTest.kt`, `MileageSubscriptionTest.kt`, `MileageStateScopeTest.kt`:
  projection, subscription and queued-state regression coverage.

Production paths above are relative to
`app/src/main/java/com/example/carcareprototype/`; tests are under
`app/src/test/java/com/example/carcareprototype/car/`.
