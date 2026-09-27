# ast-index Rules

## Search Rules

1. Use ast-index for code search: symbols, usages, implementations, hierarchies.
2. When ast-index returns usages or implementations, treat them as the answer; don't re-run the same search with grep to double-check.
3. Use grep when:
   - ast-index returns empty results
   - Searching for regex patterns (ast-index uses literal match)
   - Searching for string literals inside code (`"some text"`)
   - Searching in comments content

## Why ast-index

ast-index is 17-69x faster than grep (1-10ms vs 200ms-3s) and returns structured, accurate results.

## Command Reference

| Task | Command | Time |
|------|---------|------|
| Universal search | `ast-index search "query"` | ~10ms |
| Find class | `ast-index class "ClassName"` | ~1ms |
| Find usages | `ast-index usages "SymbolName"` | ~8ms |
| Find implementations | `ast-index implementations "Interface"` | ~5ms |
| Call hierarchy | `ast-index call-tree "function" --depth 3` | ~1s |
| Class hierarchy | `ast-index hierarchy "ClassName"` | ~5ms |
| Find callers | `ast-index callers "functionName"` | ~1s |
| Module deps | `ast-index deps "module-name"` | ~10ms |
| File outline | `ast-index outline "File.kt"` | ~1ms |

## Android-Specific Commands

| Task | Command |
|------|---------|
| Dagger provides | `ast-index provides "Type"` |
| Dagger inject | `ast-index inject "Type"` |
| Composables | `ast-index composables` |
| Suspend functions | `ast-index suspend` |
| Flows | `ast-index flows` |
| XML usages | `ast-index xml-usages "ViewClass"` |

## Index Management

- `ast-index rebuild` — Full reindex (run once after clone)
- `ast-index update` — After git pull/merge
- `ast-index stats` — Show index statistics
