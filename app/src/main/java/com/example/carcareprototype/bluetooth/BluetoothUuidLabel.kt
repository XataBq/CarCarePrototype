package com.example.carcareprototype.bluetooth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carcareprototype.bluetooth.uuids.BluetoothUuidCatalog
import com.example.carcareprototype.bluetooth.uuids.UuidMeaning

@Composable
internal fun BluetoothUuidLabel(
    uuid: String,
    meaning: UuidMeaning?,
    modifier: Modifier = Modifier,
    properties: String? = null
) {
    Column(modifier.padding(vertical = 3.dp)) {
        Text(
            text = BluetoothUuidCatalog.displayName(meaning) +
                (properties?.let { " [$it]" } ?: ""),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = BluetoothUuidCatalog.description(meaning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = uuid,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
