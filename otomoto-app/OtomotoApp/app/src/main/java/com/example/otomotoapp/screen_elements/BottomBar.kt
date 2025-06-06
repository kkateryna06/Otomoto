package com.example.otomotoapp.screen_elements

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.otomotoapp.AppBarsViewModel
import com.example.otomotoapp.Screen

@Composable
fun BottomBar(appBarsViewModel: AppBarsViewModel, screen: Screen, navController: NavHostController) {
    val context = LocalContext.current

    if (screen == Screen.CarDetailsScreen) {
        CarDetailsBottomBar(
            context,
            appBarsViewModel.carLink ?: "",
            appBarsViewModel.carPrice ?: 0
        )
    }
    else if (screen == Screen.FilterScreen) {
        FiltersBottomBar(navController)
    }
    else if (screen == Screen.FavouriteCarsScreen) {
        FavouriteCarsBottomBar(navController)
    }
}

@Composable
fun CarDetailsBottomBar(context: Context, carLink: String, carPrice: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(carLink))
            context.startActivity(intent)
        }) {
            Text(text = "View in Otomoto")
        }
        Text(text = "$carPrice PLN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FiltersBottomBar(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = { navController.navigate(Screen.MainScreen.route) }) {
            Text(text = "Apply Filters")
        }
    }
}

@Composable
fun FavouriteCarsBottomBar(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.navigate(Screen.MainScreen.route) }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
    }
}

