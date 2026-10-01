# Map progress is stored as a filled count, not per Cell

Cells fill in a fixed fill order and every Map's mask lives in code, so the only state a Map has is how many Cells are filled. We store one row per Map (`mapId`, `filledCount`) and derive every Cell from the mask, instead of one row per Cell with a `filledAt` timestamp: a Log becomes one upsert, a new Map needs no seeding (a missing row means nothing filled), and the stored progress can never drift from the mask in code.

## Considered Options

- **A row per Cell with `filledAt`** (the original schema): the timestamp was never read, and it could not serve a future statistics screen anyway, since it loses the amount of Activity per Log and cannot be recomputed once the Rate changes.
- **Recording Log history now** (`mapId`, `amount`, `loggedAt`) without showing it: rejected as premature; Log history arrives together with the statistics screen, accepting that Logs made before then are not recorded.

## Consequences

Painting, which keeps each Cell's exact pencil coverage, will need its own storage for strokes; this schema deliberately does not anticipate it.

A Map whose mask later loses Cells can be left with a stored count past its last Cell; such a Map simply reads as complete.
