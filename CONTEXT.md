# MapMethod

MapMethod fills a grid map of a country Cell by Cell from physical activity, like shading squares of a paper notebook in pencil.

## Language

**Map**:
A grid representation of a country together with the user's progress on it; one country is one Map, and what Activity it tracks is up to the user.
_Avoid_: board, drawing, level, map template

**Cell**:
The smallest fillable unit of the Map; Cells fill in a fixed fill order, and the next ones to be filled are previewed.
_Avoid_: tile, square, pixel

**Activity**:
What the user counts to earn Cells, such as push-ups or kilometres run.
_Avoid_: exercise, sport, metric

**Rate**:
How much Activity one Cell costs, chosen by the user (10 push-ups per Cell); Activity left over below the Rate carries over, it is never lost.
_Avoid_: price, cost, ratio

**Log**:
A manual record of an amount of Activity that fills the next empty Cells.
_Avoid_: achievement, workout, set

**Painting**:
Filling a single Cell by hand-shading it in pencil with a finger; the Cell keeps exactly the coverage it was painted with, gaps and strokes over its edge included.
_Avoid_: coloring, drawing, shading

**Completion**:
The moment the last Cell of a Map is filled: the pencil-grey Map turns into the country's flag colours and confetti fires. The only celebrated moment.
_Avoid_: finish, win, victory

**Start**:
The greeting entry screen that navigates one-way to Atlas.
_Avoid_: starting screen, welcome, home

**Atlas**:
The collection of Maps the user leafs through one at a time to choose which Map to open; each Map is shown as a preview of its real progress in pencil, or in its flag colours after Completion. Going back from a Map returns to Atlas.
_Avoid_: menu, map picker, level select, gallery
