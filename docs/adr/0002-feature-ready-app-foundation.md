---
status: accepted
date: 2026-09-18
---

# Verify the app composition boundary with one feature

MapMethod uses Now in Android as an architectural reference without copying its product features or full module graph. The selected scope is to prove that `:app` can assemble feature API and implementation modules by extracting the existing temporary Second screen into `:feature:second:api` and `:feature:second:impl`. The user confirmed the complete scope on 2026-09-18 and explicitly deferred implementation. Start implementation only upon a subsequent user instruction.

This extends ADR 0001's destination ownership: Home remains in `:app`, while the Second destination key moves to its feature API and its screen and entry registration move to its feature implementation. ADR 0001's single-stack behavior, restoration guarantees, and dedicated `:core:navigation` boundary remain unchanged.

## Composition contract

- The feature API owns the serializable Second destination key and exposes the navigation contract, without depending on `:app` or its feature implementation.
- The feature implementation depends on its API and exposes an explicit entry-registration function. The screen receives navigation callbacks; no new registry or navigation event bus is introduced.
- The app depends on both feature modules, composes their registration into its entry provider, and owns the application theme and root display.
- Keep stable entry content keys and saveable screen fields when moving registration. Moving source ownership must not change the agreed restoration behavior.
- Retain the existing Hilt setup. This extraction does not require an artificial ViewModel, repository, or DI service. Entry-scoped ViewModel ownership must be established when a real screen ViewModel is introduced.
- Make the feature conventions usable for this slice, declare the dependencies the new modules actually use, and enable Compose for the implementation module. Check the current catalog before changing it and verify any newly added dependency version with Caupain.
- Retain one-pane rendering with usable insets and keyboard input across window sizes. No navigation rail, multiple stacks, or list/detail scenes are introduced.

## Deferred components and consequences

The theme remains in `:app`; MaterialTheme is available to feature composition without a feature-to-app dependency. Extract a shared design system when features need shared custom components or tokens. Do not create empty core modules in anticipation of unknown requirements.

Persisted theme preferences, startup loading state, custom splash gating, data storage, network clients, background synchronization, remote image loading, analytics, deep links, environment flavors, and NiA's product-specific app state remain outside this slice. Their capabilities are absent until a concrete requirement justifies them. Performance profiles and specialized screenshot/coverage infrastructure are also deferred; ordinary build, lint, and behavior checks remain required.

Existing state-management library configuration is not a new architectural decision made by this extraction. Avoid unrelated convention or catalog cleanup.

## Alternatives and consequences

Preparing only conventions and documentation would introduce fewer modules but leave actual feature integration unproven. Extracting the temporary Second screen provides an executable example at the cost of two modules that may later be renamed or replaced by a product feature. This does not require splitting every future feature into API and implementation modules regardless of need.

## Completion criteria

1. Both new modules are included in the build. The app registers Second through the implementation helper and imports its key from the API; no duplicate Second screen or key remains in the app.
2. Neither feature module depends on `:app`; no speculative core modules are added. Home and the theme remain in the app.
3. Debug and optimized release builds, applicable lint, and navigation unit/instrumentation checks pass for the resulting module graph.
4. The existing forward, duplicate-current-route, root Back, field retention/disposal, and restoration contract from ADR 0001 remains intact. Verify process death and system/predictive Back on a device separately from activity recreation, recording variant and device/API.
5. Verify usable single-pane layout in a wide/resized window and with the keyboard shown. Do not claim adaptive multi-pane behavior.

This decision is accepted; implementation has not started. No new verification result is recorded by this decision.
