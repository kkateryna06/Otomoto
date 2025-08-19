package com.example.otomotoapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.otomotoapp.data.PreferencesHelper

@Composable
fun SettingsScreen(prefs: PreferencesHelper) {
    val serverUrl = remember { mutableStateOf(prefs.getServerUrl()) }
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Server URL")
        Row {
            OutlinedTextField(value = serverUrl.value, onValueChange = {serverUrl.value = it})
            OutlinedButton(onClick = { prefs.saveServerUrl(serverUrl.value)}) {
                Text("Ok")
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SettingsScreenPrev() {
//    SettingsScreen()
//}