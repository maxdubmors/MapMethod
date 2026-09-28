package dev.maxdubmors.mapmethod.feature.map

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import dev.maxdubmors.mapmethod.core.designsystem.theme.LocalNotebookPalette
import kotlin.math.abs
import kotlin.math.cos

// A piece seen almost edge-on still shows a sliver, so it never blinks out mid-tumble.
@Suppress("MayBeConst")
private val MinFaceFraction = 0.15f

private val PieceOutlineWidth = 0.5.dp

/**
 * The Completion confetti across the whole screen. It never takes touches, and it draws nothing
 * until [completion] fires.
 */
@Composable
internal fun ConfettiOverlay(completion: CompletionState, modifier: Modifier = Modifier) {
    // Each piece is a Cell cut out of the sheet, grid line and all, so white pieces show on light paper.
    val outline = LocalNotebookPalette.current.gridLine
    Canvas(modifier = modifier.onSizeChanged { completion.screenSize = it.toSize() }) {
        completion.confetti?.let { drawConfetti(it, outline) }
    }
}

private fun DrawScope.drawConfetti(confetti: Confetti, outline: Color) {
    val stroke = Stroke(width = PieceOutlineWidth.toPx())
    for (piece in confetti.pieces) {
        // Tumbling flips the square over, so it narrows to its edge and widens back to its face.
        val width = piece.side * abs(cos(piece.tumble)).coerceAtLeast(MinFaceFraction)
        val topLeft = Offset(piece.x - (width / 2f), piece.y - (piece.side / 2f))
        val size = Size(width, piece.side)
        rotate(degrees = piece.angle, pivot = Offset(piece.x, piece.y)) {
            drawRect(color = piece.color, topLeft = topLeft, size = size)
            drawRect(color = outline, topLeft = topLeft, size = size, style = stroke)
        }
    }
}
