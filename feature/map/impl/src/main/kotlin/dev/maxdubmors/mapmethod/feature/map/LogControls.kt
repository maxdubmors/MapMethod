package dev.maxdubmors.mapmethod.feature.map

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private val ControlSpacing = 12.dp
private val StepperSpacing = 8.dp
private val LogButtonHeight = 56.dp
private val LogIconSize = 24.dp
private val LogIconSpacing = 8.dp

@Composable
internal fun LogControls(
    count: Int?,
    remaining: Int,
    entryText: String,
    onEntryTextChange: (String) -> Unit,
    onLogCount: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = remaining > 0
    val stepBy = { amount: Int ->
        onEntryTextChange(stepLogCount(current = count, step = amount, remaining = remaining).toString())
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ControlSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(StepperSpacing),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            StepButton(
                iconRes = R.drawable.feature_map_impl_ic_remove,
                contentDescription = stringResource(R.string.feature_map_impl_step_minus_one),
                enabled = enabled,
                onClick = { stepBy(-1) },
                modifier = Modifier.testTag("stepMinus1"),
            )
            OutlinedTextField(
                value = entryText,
                onValueChange = { typed ->
                    if (typed.all(Char::isDigit)) onEntryTextChange(clampEntryText(typed, entryText, remaining))
                },
                label = { Text(text = stringResource(R.string.feature_map_impl_log_count_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = enabled,
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag("logField"),
            )
            StepButton(
                iconRes = R.drawable.feature_map_impl_ic_add,
                contentDescription = stringResource(R.string.feature_map_impl_step_plus_one),
                enabled = enabled,
                onClick = { stepBy(1) },
                modifier = Modifier.testTag("stepPlus1"),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(StepperSpacing, Alignment.CenterHorizontally)) {
            for (amount in ChipAmounts) {
                AssistChip(
                    onClick = { stepBy(amount) },
                    label = { Text(text = "+$amount") },
                    enabled = enabled,
                    modifier = Modifier.testTag("chipPlus$amount"),
                )
            }
        }
        LogButton(count = count, onLogCount = onLogCount)
    }
}

private val ChipAmounts = listOf(5, 10)

@Composable
private fun StepButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalIconButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        Icon(painter = painterResource(iconRes), contentDescription = contentDescription)
    }
}

@Composable
private fun LogButton(count: Int?, onLogCount: (Int) -> Unit) {
    Button(
        onClick = { count?.let(onLogCount) },
        enabled = count != null,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = LogButtonHeight)
                .testTag("logButton"),
    ) {
        Icon(
            painter = painterResource(R.drawable.feature_map_impl_ic_log),
            contentDescription = null,
            modifier = Modifier.size(LogIconSize),
        )
        Spacer(modifier = Modifier.width(LogIconSpacing))
        AnimatedContent(
            targetState = count,
            transitionSpec = { countTransition() },
            label = "logCount",
        ) { shown ->
            Text(
                text =
                    if (shown != null) {
                        stringResource(R.string.feature_map_impl_log_push_ups, shown)
                    } else {
                        stringResource(R.string.feature_map_impl_log_push_ups_empty)
                    },
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

/** A rising count rolls in from below, a falling one from above, like an odometer. */
private fun AnimatedContentTransitionScope<Int?>.countTransition(): ContentTransform {
    val direction = if ((targetState ?: 0) > (initialState ?: 0)) 1 else -1
    return (slideInVertically { height -> direction * height } + fadeIn())
        .togetherWith(slideOutVertically { height -> -direction * height } + fadeOut())
        .using(SizeTransform(clip = false))
}
