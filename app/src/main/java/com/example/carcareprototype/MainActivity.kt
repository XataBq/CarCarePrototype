package com.example.carcareprototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.carcareprototype.bluetooth.BluetoothScreen
import com.example.carcareprototype.diagnostics.DiagnosticRepository
import com.example.carcareprototype.presentation.DiagnosticScreen
import com.example.carcareprototype.ui.theme.CarCarePrototypeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        DiagnosticRepository.log("Phone activity created")

        enableEdgeToEdge()
        setContent {
            CarCarePrototypeTheme {
                CarTesterApp()
            }
        }
    }
}

@Composable
private fun CarTesterApp() {

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("\uD83D\uDE97") },
                    label = { Text("Android Auto") },
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("ᛒ") },
                    label = { Text("Bluetooth") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> DiagnosticScreen()
                1 -> BluetoothScreen()
            }
        }
    }
}