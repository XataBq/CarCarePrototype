package com.example.carcareprototype.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.carcareprototype.diagnostics.DiagnosticRepository

@Composable
internal fun DiagnosticScreen(vm: DiagnosticViewModel = viewModel()) {

    val state by vm.state
        .collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Car Connection Tester",
                style = MaterialTheme.typography.headlineSmall
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text("Android Auto")

                    Text(
                        text = if (state.androidAutoConnected)
                            "Car session active"
                        else
                            "Not connected"
                    )

                    HorizontalDivider()

                    Text("Odometer")

                    Text(
                        text = state.mileageKm
                            ?.let { "%.1f km".format(it) }
                            ?: "No data",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "Status: ${state.mileageStatus}"
                    )

                    Text(
                        text = "Timestamp: " +
                            (state.mileageTimestampMillis?.toString() ?: "N/A")
                    )

                    Text(
                        text = "Source: Android Auto Car Hardware API"
                    )
                }
            }

            Text(
                text = "Diagnostic logs",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.logs.asReversed()) { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Button(
                onClick = {
                    DiagnosticRepository.log(
                        "Manual diagnostic event"
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test logging")
            }
        }
    }
}