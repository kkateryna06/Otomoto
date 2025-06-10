package com.example.otomotoapp.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.otomotoapp.AppBarsViewModel
import com.example.otomotoapp.MainViewModel
import com.example.otomotoapp.R
import com.example.otomotoapp.data.CarSpecs
import com.example.otomotoapp.database.FavouriteCar
import com.example.otomotoapp.database.FavouriteCarsViewModel
import com.example.otomotoapp.screen_elements.DropDownMenu

@Composable
fun CarDetailsScreen(carId: String, viewModel: MainViewModel,
                     favCarsViewModel: FavouriteCarsViewModel,
                     appBarsViewModel: AppBarsViewModel, isSpecialCarEnabled: Boolean,
                     navController: NavHostController) {
    val carPhotos by viewModel.carPhotosLiveData.observeAsState()
    val carPhoto = carPhotos?.get(carId)

    val favCarsList by favCarsViewModel.favouriteCars.collectAsState()
    val isFavCar = favCarsList.contains(FavouriteCar(carId.toLong()))

    LaunchedEffect(carId) {
        viewModel.getCarById(carId)
        viewModel.getPhotoById(carId)
    }

    val carSpecs by viewModel.carSpecs.observeAsState()

    LaunchedEffect(carId) {
    }

    Box(modifier = Modifier.fillMaxSize()) {
        carSpecs?.let { car ->
            appBarsViewModel.updateBottomInfo(
                link = car.link ?: "",
                price = car.price
            )
            CarDetails(carSpecs = car, carPhoto, favCarsViewModel, isFavCar)
        }

    }
}

@Composable
fun CarDetails(carSpecs: CarSpecs, carPhoto: Bitmap?, favCarsViewModel: FavouriteCarsViewModel,
               isFavCar: Boolean) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
        .verticalScroll(rememberScrollState())
    ) {
        if (carPhoto != null) {
            Image(
                bitmap = carPhoto.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(300.dp)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.no_image),
                contentDescription = "Placeholder image"
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${carSpecs.mark} ${carSpecs.model} ${carSpecs.version ?: ""} (${carSpecs.year})",
                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 40.dp)
            )
            IconButton(onClick = {
                if (isFavCar) {
                    favCarsViewModel.deleteFavCar(carSpecs.car_id.toLong())
                }
                else {
                    favCarsViewModel.addFavCar(carSpecs.car_id.toLong())
                }
            }, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(
                    painter = painterResource(if(isFavCar) R.drawable.favourite else R.drawable.favourite_border),
                    contentDescription = null
                )
            }
        }


        Spacer(modifier = Modifier.height(10.dp))

        DropDownMenu(carSpecs = carSpecs, textMenu = "Basic", isDropDownMenuExpanded = true)
        DropDownMenu(carSpecs = carSpecs, textMenu = "Specification", isDropDownMenuExpanded = true)


        DropDownMenu(carSpecs = carSpecs, textMenu = "Description")

        DropDownMenu(carSpecs = carSpecs, textMenu = "Location", isDropDownMenuExpanded = true)
    }
}
