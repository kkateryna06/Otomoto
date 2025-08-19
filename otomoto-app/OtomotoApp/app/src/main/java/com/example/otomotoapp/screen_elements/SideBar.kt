package com.example.otomotoapp.screen_elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.otomotoapp.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SideBar(navController: NavHostController, drawerState: DrawerState, scope: CoroutineScope) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.7f)
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp)
    ) {
        Text("Menu", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(20.dp))
        Text("My favourites",
            modifier = Modifier.clickable {
                navController.navigate(Screen.FavouriteCarsScreen.route)
                scope.launch { drawerState.close() }
            }.fillMaxWidth())
        Text("Settings",
            modifier = Modifier.clickable {
                navController.navigate(Screen.SettingsScreen.route)
                scope.launch { drawerState.close() }
            }.fillMaxWidth())
    }
}