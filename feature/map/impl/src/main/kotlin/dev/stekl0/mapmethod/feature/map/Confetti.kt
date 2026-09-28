package dev.stekl0.mapmethod.feature.map

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import dev.stekl0.mapmethod.core.model.Flag
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

@Suppress("MayBeConst")
private val PieceCount = 150

/**
 * A frame never advances Completion's motion further than this, so a long pause (the app in the
 * background, a debugger, a janky frame) cannot throw it ahead.
 */
@Suppress("MayBeConst")
internal val MaxFrameMillis = 40L

// A safety net: the confetti ends by then even if a piece somehow stays on screen.
@Suppress("MayBeConst")
private val MaxAgeMillis = 6_000L

// The burst fires from the upper middle of the screen.
@Suppress("MayBeConst")
private val OriginHeightFraction = 0.4f

// Speeds are in screen sizes per millisecond, so the burst looks the same on any screen.
@Suppress("MayBeConst")
private val MinLaunchSpeed = 1.0e-3f

@Suppress("MayBeConst")
private val MaxLaunchSpeed = 2.0e-3f

// Half the opening angle of the upward cone the pieces fly out in.
@Suppress("MayBeConst")
private val SpreadRadians = (55.0 * PI / 180.0).toFloat()

// Screen heights per millisecond squared.
@Suppress("MayBeConst")
private val GravityPerHeight = 2.0e-6f

// Air drag: the share of speed kept is exp(-drag * millis).
@Suppress("MayBeConst")
private val DragPerMilli = 2.5e-3f

// A piece's side as a share of the screen's shorter side.
@Suppress("MayBeConst")
private val MinSideFraction = 1f / 70f

@Suppress("MayBeConst")
private val MaxSideFraction = 1f / 45f

// Either way.
@Suppress("MayBeConst")
private val MaxSpinDegreesPerMilli = 0.5f

// Radians per millisecond: how fast a piece flips over, showing its edge and then its face again.
@Suppress("MayBeConst")
private val MinTumbleSpeed = 0.004f

@Suppress("MayBeConst")
private val MaxTumbleSpeed = 0.012f

/**
 * One square piece of confetti, a small Cell cut out of the Map.
 *
 * @property x the piece's centre, in pixels from the screen's left.
 * @property y the piece's centre, in pixels from the screen's top.
 * @property vx horizontal speed, in pixels per millisecond.
 * @property vy vertical speed, in pixels per millisecond; negative is up.
 * @property side the side of the square, in pixels.
 * @property angle rotation in the screen's plane, in degrees.
 * @property spin how fast [angle] turns, in degrees per millisecond.
 * @property tumble how far the piece has flipped over, in radians.
 * @property tumbleSpeed how fast [tumble] turns, in radians per millisecond.
 * @property color the piece's colour.
 */
internal data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val side: Float,
    val angle: Float,
    val spin: Float,
    val tumble: Float,
    val tumbleSpeed: Float,
    val color: Color,
)

/**
 * Confetti on a screen of [bounds]: the [pieces] still on it, [ageMillis] after the burst. Pieces
 * are only fired by [confettiBurst]; [step] moves them and drops the ones that left the screen.
 */
internal data class Confetti(
    val pieces: List<ConfettiPiece>,
    val bounds: Size,
    val ageMillis: Long,
) {
    /** The effect has ended: every piece has left the screen. */
    val isOver: Boolean get() = pieces.isEmpty() || (ageMillis >= MaxAgeMillis)
}

/** Fires one burst of square pieces in [colors] up from the middle of a screen of [bounds]. */
internal fun confettiBurst(bounds: Size, colors: List<Color>, random: Random): Confetti {
    if (bounds.isEmpty() || colors.isEmpty()) return Confetti(pieces = emptyList(), bounds = bounds, ageMillis = 0L)
    val shorterSide = bounds.minDimension
    val pieces =
        List(PieceCount) {
            val direction = random.nextFloat(-SpreadRadians, SpreadRadians)
            val speed = random.nextFloat(MinLaunchSpeed, MaxLaunchSpeed)
            ConfettiPiece(
                x = bounds.width / 2f,
                y = bounds.height * OriginHeightFraction,
                vx = sin(direction) * speed * bounds.width,
                vy = -cos(direction) * speed * bounds.height,
                side = shorterSide * random.nextFloat(MinSideFraction, MaxSideFraction),
                angle = random.nextFloat(0f, 360f),
                spin = random.nextFloat(-MaxSpinDegreesPerMilli, MaxSpinDegreesPerMilli),
                tumble = random.nextFloat(0f, (2.0 * PI).toFloat()),
                tumbleSpeed = random.nextFloat(MinTumbleSpeed, MaxTumbleSpeed),
                color = colors.random(random),
            )
        }
    return Confetti(pieces = pieces, bounds = bounds, ageMillis = 0L)
}

/**
 * Moves the confetti on by one frame of [frameMillis]: pieces slow down in the air, fall under
 * gravity, spin and tumble. The frame is clamped, so a clock that jumps ahead or back never throws
 * the pieces around. Pieces that left the screen are dropped; none are ever added.
 */
internal fun Confetti.step(frameMillis: Long): Confetti {
    val millis = frameMillis.coerceIn(0L, MaxFrameMillis)
    if (millis == 0L) return this
    val dt = millis.toFloat()
    val speedKept = exp(-DragPerMilli * dt)
    val gravity = GravityPerHeight * bounds.height
    val moved =
        pieces.mapNotNull { piece ->
            val vx = piece.vx * speedKept
            val vy = (piece.vy * speedKept) + (gravity * dt)
            val next =
                piece.copy(
                    x = piece.x + (vx * dt),
                    y = piece.y + (vy * dt),
                    vx = vx,
                    vy = vy,
                    angle = piece.angle + (piece.spin * dt),
                    tumble = piece.tumble + (piece.tumbleSpeed * dt),
                )
            next.takeIf { it.isOnScreen(bounds) }
        }
    return Confetti(pieces = moved, bounds = bounds, ageMillis = ageMillis + millis)
}

// Pieces flying over the top come back down; only the sides and the bottom are ways out.
private fun ConfettiPiece.isOnScreen(bounds: Size): Boolean =
    ((y - side) <= bounds.height) && ((x + side) >= 0f) && ((x - side) <= bounds.width)

private fun Random.nextFloat(from: Float, until: Float): Float = from + (nextFloat() * (until - from))

/** The confetti of Completion: small Cells in the colours of the Map's [flag]. */
internal fun confettiColors(flag: Flag): List<Color> = flag.bands.map { Color(it) }
