package com.example.otomotoapp.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.otomotoapp.MainViewModel
import com.example.otomotoapp.database.FavouriteCarsViewModel

@Composable
fun FavouriteCarsScreen(viewModel: MainViewModel, favCarsViewModel: FavouriteCarsViewModel,
                        navController: NavHostController) {
    val favCarsList by favCarsViewModel.favouriteCars.collectAsState(emptyList())
    val carList by viewModel.favouriteCarsSpecsList.observeAsState(emptyList())
    val errorMessage by viewModel.errorMessage.observeAsState("")
    val isSpecialCarEnabled by viewModel.isSpecialCarEnabled.observeAsState(false)


    if (favCarsList.isNotEmpty()) {
        LaunchedEffect(favCarsList, isSpecialCarEnabled) {
            if (favCarsList.isNotEmpty()) {
                viewModel.fetchFavouriteCarsSpecs(favCarsList, isSpecialCarEnabled)
            }
        }

        Log.d("DEBUG", "fav cars: $favCarsList")
        Log.d("DEBUG", "car list: $carList")

        if (errorMessage?.isNotEmpty() == true) {
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = errorMessage!!, color = Color.Red)
            }
        }
        Column(modifier = Modifier.fillMaxSize().padding(top = 150.dp)) {

            CarAd(navController, carList, isSpecialCarEnabled, favCarsList, favCarsViewModel)
        }
    }

    else {
        Column(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Ups... It's empty", style = MaterialTheme.typography.headlineLarge)
            Log.d("DEBUG", "empty list")
        }
    }

}