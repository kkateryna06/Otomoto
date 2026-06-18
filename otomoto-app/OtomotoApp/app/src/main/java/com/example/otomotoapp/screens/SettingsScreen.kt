package com.example.otomotoapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.otomotoapp.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val currentServerUrl by viewModel.serverUrl.collectAsState()
    val serverUrl = remember { mutableStateOf(currentServerUrl) }

    LaunchedEffect(currentServerUrl) {
        serverUrl.value = currentServerUrl
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Server URL", style = MaterialTheme.typography.titleLarge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            OutlinedTextField(
                value = serverUrl.value,
                onValueChange = { serverUrl.value = it },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = { viewModel.updateServerUrl(serverUrl.value) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text("Save")
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SettingsScreenPrev() {
//    SettingsScreen()
//}
