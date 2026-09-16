package dev.stekl0.mapmethod.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeScreen(onOpenSecond: () -> Unit) {
    var testField by rememberSaveable { mutableStateOf("") }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Home screen",
            style = MaterialTheme.typography.headlineMedium,
        )
        OutlinedTextField(
            value = testField,
            onValueChange = { testField = it },
            label = { Text("Home test field") },
            modifier = Modifier.testTag("home-test-field"),
        )
        Button(
            onClick = onOpenSecond,
            modifier = Modifier.testTag("open-second-button"),
        ) {
            Text("Open second screen")
        }
    }
}
