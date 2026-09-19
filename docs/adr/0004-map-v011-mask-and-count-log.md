# Map v0.1.1: 60x60 mask with count-based Log

v1 shipped a 30x30 Poland mask (635 Cells) with one Cell per fixed 10 push-up Log, but the map was not recognizable and logging was rigid. For v0.1.1 we fix a 60x60 mask over the same bounding box (total floats, ~2.5k Cells) with `1 Cell = 1 push-up`, and replace the single button with a numeric field plus ±1/±5/±10 steppers driving `MapRepository.logPushUps(count): Int` via a single-statement multi-fill, because accuracy comes from data and effort entry must accept any N.

## Consequences

- `CONTEXT.md` Cell/Log entries updated; ADR 0003 mask-size and Cell/Log lines superseded, rest of v1 scope (flag bands by row-half, 1x–4x clamped transient zoom, single-pane, no reset/expand) unchanged.
- DB `version = 2` with auto-migration and schema export; migration preserves filled count K in N-S/W-E order.
- Input clamps to `1..remaining` on every change, cleared is invalid with steppers restarting from `1`, button `Log N push-ups` disabled when full/invalid, state `rememberSaveable`.
- Done gate is the v1 gate plus `android studio analyze-file <path>` for every affected file and a side-by-side screenshot gate (north coast, eastern protrusion, southern edge legible); mask strings approved in review.
- `versionName 0.1.1`, `versionCode` incremented by 1 on every `versionName` change.
