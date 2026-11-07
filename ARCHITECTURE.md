# Architecture and refactoring requirements

Review date: 2026-11-07. Scope: all production Kotlin sources, resources/build
entry points and existing regression tests in this prototype.

## Requirements

1. **Responsibilities and dependencies.** Compose renders state and emits actions.
   ViewModels coordinate screen state. Repositories own diagnostics and transport
   state; platform adapters own Android calls. Data must not import presentation.
   Framework lifecycle entry points are allowed to access their application services.
2. **MVVM and unidirectional flow.** User actions travel to ViewModel methods;
   immutable state travels back through lifecycle-aware Flow collection. Android
   permission/folder launchers remain in the UI, while their results update the
   shared state through explicit actions.
3. **Single source of truth.** Each fact has one mutable owner. A ViewModel exposes
   repository state without a second mutable copy. Presentation projections and
   durable journal snapshots are derived views, not independent mutable owners.
4. **Encapsulation and SOLID.** Mutable flows and resource handles are private.
   Small contracts isolate presentation from concrete platform implementations.
   Constructors accept collaborators where substitution is useful. Avoid inheritance,
   new modules, one-use-case-per-method wrappers and generic base classes without
   an actual need. OCP is supported by replaceable implementations; LSP requires
   those implementations to preserve the contract, not merely the method names.
5. **Structured concurrency and ownership.** One owner registers/unregisters each
   listener and manages its deadline. Stop/close are safe to repeat. Old callbacks
   and timers cannot change a newer operation. Cancellation is propagated.
6. **No redundant work.** Do not duplicate permission processing, combine/re-store
   the same state in multiple layers, reformat car templates for log-only changes,
   or query the document provider twice when a single validated operation suffices.
7. **Readable code.** Names describe the real feature. Extract independent workflows
   and large dialogs; retain local UI-only input state locally. Use named timeouts,
   exhaustive models and explicit dependencies instead of hidden side effects.
8. **Compatibility.** Preserve Android Auto mileage isolation, UNVERIFIED labeling,
   JSONL history, last-100 loading, session export, append/replace conflict handling,
   Bluetooth operations and permission behavior. Refactoring must not imply that
   unverified mileage is a total odometer or that hardware was tested.
9. **Verification.** Build and lint the complete app; run regression tests for state,
   subscription ownership and storage. Use fake dependencies to test changed
   boundaries. Hardware behavior requires a connected phone/head unit and is
   reported separately from local checks.

These requirements follow the Android [architecture recommendations](https://developer.android.com/topic/architecture/recommendations)
and [UI layer guidance](https://developer.android.com/topic/architecture/ui-layer).
They apply to responsibilities and behavior rather than requiring a specific folder
name or a multi-module project.

## Findings and implemented changes

| Finding | Change |
| --- | --- |
| Diagnostic repository imported a presentation model | Move DiagnosticState into diagnostics; data no longer imports phone presentation |
| Phone UI directly mutated repository and global command services | Route actions through injectable DiagnosticViewModel |
| ViewModels constructed/owned platform workflows | Plain ViewModels with constructor dependencies and Android factories at composition boundaries |
| SAF, preferences, IO and timeout workflow lived inside the diagnostic ViewModel | Extract CarLogExportOperations / CarLogExportService; VM keeps dialog state |
| Large export dialogs coupled to the VM | Stateless CarLogExportDialogs receives values and callbacks |
| Permission grant/denial duplicated across Compose fields and a global side channel | One immutable MileagePermissionState exposed by StateFlow; UI observes it |
| Car permission callback processed results both directly and through its Flow collector | Only the Flow collector restarts the subscription |
| Every log append formatted car-template strings/date | Distinct by connection/mileage before mapping; phone projection memoized by those inputs |
| Template navigation enum disagreed with actual tabs; selection lost on recreation | Real destinations used by navigation, saved with rememberSaveable |
| Bluetooth transport deadlines lived in ViewModel, which re-combined repository state | Repository owns deadlines and one BluetoothUiState; VM exposes its StateFlow |
| Bluetooth scanner could deliver old callbacks to a new operation | BLE ScanCallback identity checks and repository operation identity gates |
| Bluetooth remained active while its UI was stopped; cleanup was repeated | LifecycleStartEffect and idempotent repository ownership/close |
| Export append/replace redundantly looked up its destination | Remove preliminary lookup; exporter still validates destination at write time |

## Ownership after refactoring

- DiagnosticRepository is the process-wide owner of the serialized journal and its
  read state. DiagnosticJournal is the phone-facing contract. MileageSubscription
  remains the producer of attempt state; MileageStateScope validates its published
  snapshots against connection/vehicle selection boundaries.
- CarMileagePermission owns the shared permission snapshot. The OS remains the
  authority: refresh checks the actual permission when the phone resumes.
- CarLogExportService owns SAF and IO operations. DiagnosticViewModel owns only
  export dialog state and action coordination. The factory captures application
  services, never an Activity.
- BluetoothRepository owns the transport lifecycle, deadlines and observable
  BluetoothUiState. BluetoothViewModel accepts BluetoothDiagnostics and forwards
  actions. Scanner/GATT/RFCOMM adapters remain platform-specific.
- Session and Screen are Car App Library lifecycle adapters. They deliberately
  retain access to the shared car application service; an Android ViewModel is not
  required inside a car Session.

## Deliberate boundaries

This is a single-module diagnostic app, not a claim that all Android dependencies
have been eliminated from data. Uri and platform transport adapters remain at
Android boundaries. The process-wide car repository and retry router retain their
application lifetime because the car service can run without the phone Activity.
Replacing them with a DI framework is not necessary for this refactoring.

Existing pure mileage, session, JSONL and UUID logic was reviewed and retained.
Do not introduce a second database, generic event bus or derived mutable UI cache.
New behavior should extend the small contracts and preserve ownership rules.

## Validation and remaining limits

Run using the repository wrapper and configured Android SDK/JDK:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --offline --console=plain
```

Final local result: BUILD SUCCESSFUL; 80 JVM tests, zero failures/errors; debug
APK assembled; lint has zero errors and 15 warnings. New regression tests verify
that both ViewModels observe their injected owner's state, permission and retry
actions have one route, and Bluetooth ownership is closed once. Existing mileage,
session, export, storage, Bluetooth reducer and UUID tests also pass.

Unused template colors and Home/Favorite/Profile icons were removed after reference
checks. Remaining lint warnings concern platform/dependency versions, manifest/host
configuration and KTX suggestions; upgrading dependencies is outside this change.
No instrumented tests, physical Bluetooth devices or live Android Auto/DHU were
run for this refactoring. Hardware regression checks are still required.

Bluetooth transport/store implementations are composed inside its repository;
presentation is isolated through BluetoothDiagnostics. Bluetooth history IO errors
still go to logcat as before; this refactoring does not add a durable retry policy
for that history. The car journal retains its existing serialized persistence and
error reporting. These are explicit boundaries, not a claim of complete absence
of future technical debt.
