To learn a dependency's API shape or implementation, read its sources with the ksrc CLI (`ksrc --help`) instead of opening `.gradle` directly.

## Agent skills

### Issue tracker

Issues live in GitHub Issues on `maxdubmors/MapMethod` (via the `gh` CLI). See `docs/agents/issue-tracker.md`.

### Triage labels

Default vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: one `CONTEXT.md` plus `docs/adr/` at the repo root. See `docs/agents/domain.md`.

## Screenshot tests

Modules opt in with the `mapmethod.android.screenshot` plugin; baselines live in the module's `src/test/screenshots`, recorded on Linux and committed.
A test renders the stateless screen from an explicit state with `createAndroidComposeRule<ComponentActivity>()` and the helpers of `:core:screenshot-testing`, which the plugin puts on the test classpath. `captureMultiTheme` captures `<name>_light.png`, `<name>_dark.png` and, with dynamic colour, `<name>_light_dynamic.png` and `<name>_dark_dynamic.png`; `captureMultiDevice` captures `<name>_phone.png`, `<name>_foldable.png` and `<name>_tablet.png` in the light theme without dynamic colour. Both hold the test clock still and compose every capture afresh; `settleMillis` runs an animation to the frame the baseline pins.

- `./gradlew recordRoborazziDebug` records the baselines after a deliberate visual change.
- `./gradlew verifyRoborazziDebug` checks the screens against them; a failure leaves a diff in `build/outputs/roborazzi/*_compare.png`.
- `./gradlew compareRoborazziDebug` writes those diffs without failing.

<!-- echolot -->
## Performance work: echolot

This project uses [echolot](https://github.com/grishan0v/echolot) to find where
…  (`echolot guide` prints the rest)
