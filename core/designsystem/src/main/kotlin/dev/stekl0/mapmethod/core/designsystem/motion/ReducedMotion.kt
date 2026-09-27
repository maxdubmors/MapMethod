package dev.stekl0.mapmethod.core.designsystem.motion

import android.animation.ValueAnimator

/**
 * Whether system animations are off: the animator duration scale is 0, set by the user or by
 * battery saver. Motion then shows its final state at once. Read it when motion is about to start,
 * so a change in settings applies to the next animation.
 */
public fun isReducedMotion(): Boolean = !ValueAnimator.areAnimatorsEnabled()
