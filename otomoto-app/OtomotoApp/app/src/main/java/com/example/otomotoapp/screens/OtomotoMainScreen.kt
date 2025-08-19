package com.example.otomotoapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.otomotoapp.MainViewModel
import com.example.otomotoapp.R
import com.example.otomotoapp.Screen
import com.example.otomotoapp.data.CarSpecs
import com.example.otomotoapp.data.Location
import com.example.otomotoapp.database.FavouriteCar
import com.example.otomotoapp.database.FavouriteCarsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun OtomotoMainScreen(
    viewModel: MainViewModel,
    favCarsViewModel: FavouriteCarsViewModel,
    navController: NavHostController
) {
    val carList by viewModel.carList.observeAsState(emptyList())
    val errorMessage by viewModel.errorMessage.observeAsState("")
    val isSpecialCarEnabled by viewModel.isSpecialCarEnabled.observeAsState(false)

    val favCarsList by favCarsViewModel.favouriteCars.collectAsState()

    val userFilterData by viewModel.userFilterData.collectAsState()

    LaunchedEffect(userFilterData) {
        viewModel.resetPaginationAndFetch()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (errorMessage?.isNotEmpty() == true) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        CarAd(
            navController = navController,
            adds = carList,
            isSpecialCarEnabled = isSpecialCarEnabled,
            favCarsList = favCarsList,
            favCarsViewModel = favCarsViewModel,
            viewModel = viewModel,
            onLoadMoreClick = { viewModel.fetchNextPage() }
        )
    }
}


@Composable
fun CarAd(
    navController: NavHostController,
    adds: List<CarSpecs>,
    isSpecialCarEnabled: Boolean,
    favCarsList: List<FavouriteCar>,
    favCarsViewModel: FavouriteCarsViewModel,
    viewModel: MainViewModel,
    onLoadMoreClick: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(adds.filterNotNull()) { item ->
            val isFavCar = favCarsList.contains(FavouriteCar(item.car_id.toLong()))
            AdItem(navController, item, isSpecialCarEnabled, isFavCar, favCarsViewModel, viewModel)
        }


        item(span = { GridItemSpan(2) }) {
            Button(
                onClick = onLoadMoreClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Load more")
            }
        }
    }
}


fun getDaysSince(listingDate: String, soldDate: String?): Int {
    val format = SimpleDateFormat("yyy-MM-dd", Locale.getDefault())

    val startDate = format.parse(listingDate)
    val endDate = soldDate?.let { format.parse(soldDate) } ?: Date()

    val diff = endDate.time - startDate.time
    return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
}

@Composable
fun AdItem(
    navController: NavHostController, carSpecs: CarSpecs, isSpecialCarEnabled: Boolean,
    isFavCar: Boolean, favCarsViewModel: FavouriteCarsViewModel, viewModel: MainViewModel) {

    LaunchedEffect(carSpecs.car_id) {
        viewModel.getPhotoById(carSpecs.car_id)
    }
    val carPhotos by viewModel.carPhotosLiveData.observeAsState()
    val carPhoto = carPhotos?.get(carSpecs.car_id)

    Card(
        modifier = Modifier
            .height(400.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {
                navController.navigate(
                    Screen.CarDetailsScreen.withArgs(
                        carSpecs.car_id,
                        isSpecialCarEnabled
                    )
                )
            }
    ) {
        Column()
        {
            if (carPhoto != null) {
                Image(
                    bitmap = carPhoto.asImageBitmap(), contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                )
            }
            else {
                Image(
                    painter = painterResource(id = R.drawable.no_image),
                    contentDescription = "car photo"
                )

//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .aspectRatio(1f),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
            }

            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            color = if(carSpecs.sell_date.isNullOrBlank()) Color.Green else Color.Red
                        )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "${getDaysSince(carSpecs.date, carSpecs.sell_date)}")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    if (isFavCar) {
                        favCarsViewModel.deleteFavCar(carSpecs.car_id.toLong())
                    }
                    else {
                        favCarsViewModel.addFavCar(carSpecs.car_id.toLong())
                    }
                }) {
                    Icon(
                        painter = painterResource(if(isFavCar) R.drawable.favourite else R.drawable.favourite_border),
                        contentDescription = null
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(start = 5.dp, end = 5.dp, bottom = 5.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${carSpecs.mark} ${carSpecs.model} (${carSpecs.year})",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
                )

                Column(
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.speed), contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "${carSpecs.mileage} km")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.gas_station), contentDescription = null,
                            modifier = Modifier
                                .size(25.dp)
                                .padding(start = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "${carSpecs.urban_consumption}")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.engine), contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "${carSpecs.engine_power} KM")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${carSpecs.price} PLN",
                        style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
