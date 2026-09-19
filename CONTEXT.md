# MapMethod

MapMethod fills a grid map cell by cell from physical activity.

## Language

**Map**:
A grid representation of Poland in v1, zoomable, without reset or expansion.
_Avoid_: board, drawing

**Cell**:
The smallest fillable unit of the Map; one Cell equals 10 push-ups in v1 and Cells fill row by row north-to-south, west-to-east with the next Cell previewed. Filled Cells show Polish flag colors by vertical band.
_Avoid_: tile, square

**Log**:
A manual record of 10 push-ups that fills the next empty Cell, without deduplication in v1.
_Avoid_: achievement, workout, set
