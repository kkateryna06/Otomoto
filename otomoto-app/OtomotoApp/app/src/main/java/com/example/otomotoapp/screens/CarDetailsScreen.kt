package com.example.otomotoapp.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
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
                     appBarsViewModel: AppBarsViewModel,
                     navController: NavHostController) {
    val favCarsList by favCarsViewModel.favouriteCars.collectAsState()
    val isFavCar = favCarsList.contains(FavouriteCar(carId))

    LaunchedEffect(carId) {
        viewModel.getCarById(carId)
    }

    val carSpecs by viewModel.carSpecs.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()

    LaunchedEffect(carId) {
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val car = carSpecs
        when {
            car != null -> {
                val resolvedCarId = car.id ?: carId
                LaunchedEffect(resolvedCarId) {
                    appBarsViewModel.updateBottomInfo(
                        link = car.url ?: "",
                        price = car.price
                    )
                }
                val photoUrls = car.photoUrls.orEmpty()
                    .filter { it.isNotBlank() }
                    .ifEmpty { listOfNotNull(viewModel.getPhotoUrl(car.photoPath)) }
                CarDetails(
                    carSpecs = car,
                    carId = resolvedCarId,
                    carPhotoUrls = photoUrls,
                    favCarsViewModel = favCarsViewModel,
                    isFavCar = isFavCar
                )
            }
            errorMessage?.isNotBlank() == true -> {
                Text(
                    text = errorMessage.orEmpty(),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

    }
}

@Composable
fun CarDetails(carSpecs: CarSpecs, carId: String, carPhotoUrls: List<String>, favCarsViewModel: FavouriteCarsViewModel,
               isFavCar: Boolean) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        CarPhotoGallery(
            carId = carId,
            photoPath = carSpecs.photoPath,
            photoUrls = carPhotoUrls
        )
        Spacer(modifier = Modifier.height(14.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${carSpecs.brand} ${carSpecs.model} ${carSpecs.version ?: ""} (${carSpecs.year})",
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 40.dp)
            )
            IconButton(onClick = {
                if (isFavCar) {
                    favCarsViewModel.deleteFavCar(carId)
                }
                else {
                    favCarsViewModel.addFavCar(carId)
                }
            }, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(
                    painter = painterResource(if(isFavCar) R.drawable.favourite else R.drawable.favourite_border),
                    contentDescription = null
                )
            }
        }


        Spacer(modifier = Modifier.height(14.dp))

        DropDownMenu(carSpecs = carSpecs, textMenu = "Basic", isDropDownMenuExpanded = true)
        DropDownMenu(carSpecs = carSpecs, textMenu = "Specification", isDropDownMenuExpanded = true)
        DropDownMenu(carSpecs = carSpecs, textMenu = "Price history", isDropDownMenuExpanded = true)


        DropDownMenu(carSpecs = carSpecs, textMenu = "Description")

        DropDownMenu(carSpecs = carSpecs, textMenu = "Location", isDropDownMenuExpanded = true)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarPhotoGallery(carId: String, photoPath: String?, photoUrls: List<String>) {
    if (photoUrls.isEmpty()) {
        AsyncImage(
            model = null,
            contentDescription = "car photo",
            placeholder = painterResource(id = R.drawable.no_image),
            error = painterResource(id = R.drawable.no_image),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        return
    }

    val pagerState = rememberPagerState(pageCount = { photoUrls.size })

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) { index ->
            val photoUrl = photoUrls[index]
            AsyncImage(
                model = photoUrl,
                contentDescription = "car photo ${index + 1}",
                placeholder = painterResource(id = R.drawable.no_image),
                error = painterResource(id = R.drawable.no_image),
                onSuccess = {
                    Log.d("CarPhoto", "Details image loaded: carId=$carId, index=$index, url=$photoUrl")
                },
                onError = {
                    Log.e(
                        "CarPhoto",
                        "Details image failed: carId=$carId, index=$index, photoPath=$photoPath, url=$photoUrl",
                        it.result.throwable
                    )
                },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        if (photoUrls.size > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                photoUrls.forEachIndexed { index, _ ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.LightGray
                                }
                            )
                    )
                }
            }
        }
    }
}
