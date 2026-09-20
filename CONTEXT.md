# MapMethod

MapMethod fills a grid map cell by cell from physical activity.

## Language

**Map**:
A grid representation of Poland in v1, zoomable, without reset or expansion.
_Avoid_: board, drawing

**Cell**:
The smallest fillable unit of the Map; one Cell equals 1 push-up in v1 and Cells fill row by row north-to-south, west-to-east with the next Cell previewed. Filled Cells show Polish flag colors by vertical band.
_Avoid_: tile, square

**Log**:
A manual record of push-ups that fills that many next empty Cells, without deduplication in v1.
_Avoid_: achievement, workout, set

**Start**:
The greeting entry screen that navigates one-way to Map.
_Avoid_: starting screen, welcome, home
