---
status: accepted
date: 2026-09-16
---

# Minimal Navigation 3 foundation in a dedicated module

MapMethod adapts the single-stack essentials of Now in Android (NiA) using Navigation 3. A dedicated `:core:navigation` module owns the stack and navigation operations; `:app` owns destination keys, temporary screens, and their display. A package inside `:app` would suffice technically, but the dedicated module is the explicitly selected boundary despite its additional build configuration.

This design was accepted before implementation. The foundation is now implemented and verified against the acceptance criteria below.

## Evidence and alternatives

The inspected NiA checkout is `/home/maxdubmors/Projects/nowinandroid` at commit `12f80da6518e161ed16a06a68e71fb8a873576d6`. Its production sources use Navigation 3, despite an outdated Navigation 2 reference in its agent instructions.

- `core/navigation/.../NavigationState.kt` creates a stack of top-level sections and a separate stack for each section. `toEntries` decorates and combines those stacks.
- `core/navigation/.../Navigator.kt` switches sections and resets their histories. For ordinary destinations, `remove(key); add(key)` can reorder an existing entry: `[Home, A, B]` becomes `[Home, B, A]` when opening `A`. Calling `goBack()` at the start key throws an exception.
- `app/.../ui/NiaApp.kt` connects an `entryProvider` to `NavDisplay` and adds adaptive list/detail presentation.
- Serializable keys and screen registrations live in feature API and implementation modules, respectively.
- NiA's navigator unit tests cover stack operations, including section switching. They do not establish process-death restoration or restoration of screen fields.

MapMethod was inspected at commit `033f90ec2e897ad4c32673e8fb1955fb6e37abff` with existing local application and manifest edits. Its settings include only `:app`, and `MainActivity` displays a greeting. Navigation 3 dependencies are absent from the version catalog.

The follow-up review at `64032a3` found both `:app` and `:core:navigation` registered, Navigation 3 dependencies configured, and the home and second screens connected through `MapMethodApp`. The earlier observations and build prerequisites below describe the original decision baseline, not outstanding work to repeat.

During app-foundation scoping on 2026-09-17, the user confirmed that NiA is a reference for architectural principles and selective code reuse, rather than a requirement to reproduce its file structure or visual behavior. The existing system-driven theme and dynamic color are retained; persisted theme preferences are not required for this foundation. Verification must cover both debug and optimized release builds, applicable lint, navigation tests, and the restoration and system Back scenarios below.

Keeping navigation inside `:app` would minimize files, public API, and Gradle configuration. Copying NiA's entire navigation module would instead introduce multiple-stack behavior that this slice does not need. The chosen module retains a narrow single-stack responsibility without that behavior.

NiA also uses the standard `NavBackStack` created by `rememberNavBackStack`. Its custom `NavigationState` coordinates multiple standard stacks; it is not a replacement stack implementation. MapMethod needs one history, so `Navigator` owns one private `NavBackStack` and exposes a read-only `List<NavKey>` for rendering. Introduce a NiA-like state holder when actual top-level sections require independently retained histories.

## Minimal composition

| Owner | Part | Purpose |
| --- | --- | --- |
| `:core:navigation` | One `rememberNavBackStack` and a small `Navigator` | Restore one history and provide `navigate`, `goBack`, and `canGoBack` behavior. No separate multi-stack `NavigationState` or flattening helper is required. |
| `:app` | Two `@Serializable` keys implementing `NavKey` | Identify the home and second screens and allow the history to be restored. This slice needs no route arguments. |
| `:app` | One `NavDisplay` and `entryProvider` | Map the two keys to screens and connect system Back to the navigator. |
| `:app` | Saveable state support for each navigation entry | Preserve test fields while their entry remains in the history. Use Navigation 3's saveable entry decoration and explicit stable content keys for each destination. |
| `:app` | Two temporary screens with navigation callbacks and test fields | Demonstrate transitions, state retention, and state disposal without implementing product features. |

The navigator is scoped to the navigation UI and restored stack, rather than an application singleton. Screens receive callbacks instead of owning or modifying the stack. The app supplies the starting key; `:core:navigation` has no dependency on app screens or app-specific keys.

The module uses Navigation 3 runtime and Compose runtime, with the Compose compiler configured for its composable state creation. Navigation 3 runtime is an API dependency where its types appear in the module's public contract. The app uses Navigation 3 UI and the Kotlin serialization plugin/runtime for its destination keys. Hilt, JSON-specific serialization, and a navigation ViewModel are not required by this slice. Preserve source attribution and license notices for any NiA code actually copied or adapted.

Saving the list of destination keys and saving the fields belonging to each entry are separate requirements. Android's [Navigation 3 state documentation](https://developer.android.com/guide/navigation/navigation-3/save-state) describes `rememberNavBackStack`, serializable keys, and entry-specific state ownership.

## Agreed behavior

- A fresh launch starts on the home screen. Opening the second screen appends its key to the single history.
- A request for the current key is ignored, including a repeated tap. NiA's removal and reordering of older matching keys is not adopted. Reopening a destination already deeper in the history is outside this two-screen slice.
- The second screen's Back button and a completed system Back action remove the current entry and reveal the home screen. Cancelling a predictive Back gesture leaves the history unchanged.
- The home screen has no on-screen Back button. At the root, the app leaves system Back to Android, allowing the app to leave the foreground without removing the root entry or requesting confirmation. A direct programmatic `goBack()` at the root is a no-op.
- Rotation and restoration of a saved task after system process death preserve the current destination, history, and saveable test fields. This does not promise persistence after force-stop or arbitrary loss of saved task state.
- The home screen retains its test field after forward/back navigation. Removing the second screen from the history disposes its saved field state; opening it again starts with initial values.
- Removing the task from Recents and launching a new task starts at the home screen with initial state.

## Exclusions and consequences

| Excluded part | Consequence and point of reconsideration |
| --- | --- |
| Top-level sections, navigation bar/rail, multiple stacks, and `toEntries` flattening | There is one history and no independently remembered section. Introduce these only when actual section navigation is required. |
| NiA's existing-key reordering and section reselection rules | Navigation does not rearrange older entries or reset a section. More complex repeat-destination semantics require a separate decision. |
| Per-entry ViewModel store decoration and its lifecycle dependency | The temporary screens have no ViewModels. Before adding them, establish their lifetime and add entry scoping when required; per-entry isolation and cleanup are not provided by this slice. |
| Feature API/implementation modules and per-feature registration helpers | The app declares both destinations and their registration. Revisit ownership when real feature modules are introduced. |
| Adaptive list/detail scenes and metadata | One destination is shown at a time, including in wide windows. Simultaneous list/detail display needs additional design. |
| Deep links, route arguments, and navigation results | Only in-app transitions between the two parameterless destinations are supported. External entry paths, argument restoration, and results need separate contracts. |
| Navigation DI, global event delivery, and the full `NiaAppState` | No navigation service or event bus is introduced. News data, unread indicators, connectivity, timezone handling, and telemetry are not part of navigation ownership. |
| Custom transitions and the full NiA test suite | Use standard Navigation 3 transitions and tests for the agreed behavior. No NiA visual parity or coverage of its section-specific behavior is claimed. |

The existing `AndroidFeatureApiConventionPlugin` references `:core:navigation`, but is not currently applied to a feature module. That reference is compatible with the selected module name; it was not evidence that a separate module was technically necessary. Activating feature plugins later still requires checking their other dependencies.

## Build prerequisites

Register `:core:navigation` and connect it to `:app`. Add the required Navigation 3 and serialization catalog entries and plugin configuration, keeping destination serialization in the app. Resolve the missing `kotlin.test` and `androidx.tracing.ktx` aliases referenced by the existing Android library convention plugin if that plugin is used for the new module. Limit build changes to what is needed for this slice.

NiA's inspected catalog pins Navigation 3 to `1.0.0` and Kotlin to `2.3.0`; MapMethod currently declares Kotlin `2.4.20`. These are source observations, not a verified compatible dependency selection. Choose and validate the dependency versions during implementation instead of copying NiA's entire catalog or changing the project's toolchain speculatively.

## Acceptance criteria for implementation

1. A fresh launch displays the home screen. Opening the second screen works; repeated requests for the current destination do not create duplicates. Unit tests cover the navigator's forward, repeat, back, and root no-op rules.
2. The second screen's Back button and system Back each return exactly one screen. A cancelled predictive Back gesture changes nothing. System Back on the home screen leaves the foreground through Android's normal handling without an exception or empty stack.
3. Rotation and restoration of a saved task after system process death each restore the second screen, the preceding home entry, and test field values. Returning home after restoration reveals its saved field value.
4. A home field survives a round trip. A second-screen field is discarded after Back removes that entry, and reopening the second screen shows its initial value.
5. Removing the task from Recents and launching again displays the home screen with initial values.
6. Both debug and optimized release app builds, including the navigation module, succeed; applicable lint checks and navigation unit/UI tests pass. Exercise process death separately by backgrounding the app, killing its background process without force-stop or task removal, and restoring the task. Activity recreation or a saved-state restoration test alone is not proof of this scenario. Record the build variant, device/API, and results for system Back, gesture cancellation, process restoration, and fresh-task checks.

## Implementation and verification record

The decision remains accepted and the implementation is complete. Commit `64032a3` implements the single-stack foundation; the follow-up application change assigns stable Navigation 3 entry content keys so saveable fields restore after process death.

During verification on 2026-09-17, the following commands passed on the working tree containing `64032a3` and subsequent local application/test edits:

- `./gradlew :app:check :core:navigation:check :app:compileDebugAndroidTestKotlin --no-daemon`
- `./gradlew test --no-daemon`
- `./gradlew :app:connectedDebugAndroidTest --no-daemon`
- `./gradlew :app:assembleRelease :core:navigation:assembleRelease :app:lintRelease :core:navigation:lintRelease --no-daemon`

The instrumentation suite passed all six tests on `Medium_Phone` with API 37.1. The release app and navigation module builds and release lint also passed.

Manual verification on the same `Medium_Phone` / API 37.1 passed for Home and Second navigation, Home field round-trip retention, Second field disposal after pop, system Back, cancelled and completed predictive Back, and Recents removal followed by a fresh Home task. After backgrounding the task and killing the app process through `run-as` without force-stop or task removal, the Second destination and both saveable test fields restored; returning Home revealed its saved field. The root system Back left the app in the launcher without an empty-stack failure.
