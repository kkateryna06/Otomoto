package com.example.otomotoapp.screens

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
import com.example.otomotoapp.MainViewModel
import com.example.otomotoapp.R
import com.example.otomotoapp.Screen
import com.example.otomotoapp.data.CarSpecs
import com.example.otomotoapp.database.FavouriteCar
import com.example.otomotoapp.database.FavouriteCarsViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun OtomotoMainScreen(
    viewModel: MainViewModel,
    favCarsViewModel: FavouriteCarsViewModel,
    navController: NavHostController
) {
    val carList by viewModel.carList.observeAsState(emptyList())
    val errorMessage by viewModel.errorMessage.observeAsState("")

    val favCarsList by favCarsViewModel.favouriteCars.collectAsState()

    val appliedFilterData by viewModel.appliedFilterData.collectAsState()

    LaunchedEffect(appliedFilterData) {
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
    favCarsList: List<FavouriteCar>,
    favCarsViewModel: FavouriteCarsViewModel,
    viewModel: MainViewModel,
    onLoadMoreClick: (() -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(adds.filterNotNull()) { item ->
            val isFavCar = favCarsList.contains(FavouriteCar(item.id))
            AdItem(navController, item, isFavCar, favCarsViewModel, viewModel)
        }


        if (onLoadMoreClick != null) {
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
}


fun getDaysSince(listingDate: String, soldDate: String?): Int {
    val startDate = parseCarDate(listingDate) ?: return 0
    val endDate = soldDate
        ?.takeIf { it.isNotBlank() }
        ?.let { parseCarDate(it) }
        ?: LocalDate.now()

    return ChronoUnit.DAYS.between(startDate, endDate).toInt()
}

private fun parseCarDate(value: String): LocalDate? =
    runCatching { LocalDate.parse(value.take(10)) }
        .getOrElse {
            runCatching { LocalDateTime.parse(value).toLocalDate() }.getOrNull()
        }

@Composable
fun AdItem(
    navController: NavHostController, carSpecs: CarSpecs,
    isFavCar: Boolean, favCarsViewModel: FavouriteCarsViewModel, viewModel: MainViewModel) {

    Card(
        modifier = Modifier
            .height(400.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {
                navController.navigate(
                    Screen.CarDetailsScreen.withArgs(carSpecs.id)
                )
            }
    ) {
        Column()
        {
            AsyncImage(
                model = viewModel.getPhotoUrl(carSpecs.id),
                contentDescription = "car photo",
                placeholder = painterResource(id = R.drawable.no_image),
                error = painterResource(id = R.drawable.no_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            color = if(carSpecs.disappearedAt.isNullOrBlank()) Color.Green else Color.Red
                        )
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = "${getDaysSince(carSpecs.postedAt, carSpecs.disappearedAt)}")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    if (isFavCar) {
                        favCarsViewModel.deleteFavCar(carSpecs.id)
                    }
                    else {
                        favCarsViewModel.addFavCar(carSpecs.id)
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
                    text = "${carSpecs.brand} ${carSpecs.model} (${carSpecs.year})",
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
                        Text(text = "${carSpecs.urbanConsumption}")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.engine), contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "${carSpecs.enginePower} KM")
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
