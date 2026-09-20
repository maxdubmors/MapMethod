package dev.stekl0.mapmethod.feature.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val ScreenPadding = 24.dp
private val ContentSpacing = 16.dp

@Composable
public fun StartScreen(
    title: String,
    onShowMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(ScreenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("startTitle"),
        )
        Text(
            text = stringResource(R.string.feature_start_impl_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .padding(top = ContentSpacing)
                    .testTag("startSubtitle"),
        )
        Button(
            onClick = onShowMap,
            modifier =
                Modifier
                    .padding(top = ContentSpacing)
                    .testTag("showMapButton"),
        ) {
            Text(text = stringResource(R.string.feature_start_impl_show_map))
        }
    }
}
