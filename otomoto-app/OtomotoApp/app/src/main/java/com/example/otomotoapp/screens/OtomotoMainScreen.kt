package com.example.otomotoapp.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.otomotoapp.MainViewModel
import com.example.otomotoapp.R
import com.example.otomotoapp.Screen
import com.example.otomotoapp.data.CarSpecs
import com.example.otomotoapp.database.FavouriteCar
import com.example.otomotoapp.database.FavouriteCarsViewModel
import com.example.otomotoapp.ui.toDisplayValueOrDash
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OtomotoMainScreen(
    viewModel: MainViewModel,
    favCarsViewModel: FavouriteCarsViewModel,
    navController: NavHostController
) {
    val carList by viewModel.carList.observeAsState(emptyList())
    val errorMessage by viewModel.errorMessage.observeAsState("")
    val mockDataMessage by viewModel.mockDataMessage.observeAsState()

    val favCarsList by favCarsViewModel.favouriteCars.collectAsState()

    val appliedFilterData by viewModel.appliedFilterData.collectAsState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(appliedFilterData) {
        viewModel.refreshCarsForCurrentFiltersIfNeeded()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (errorMessage?.isNotEmpty() == true) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        if (mockDataMessage?.isNotEmpty() == true) {
            Text(
                text = mockDataMessage.orEmpty(),
                color = Color(0xFF8A5A00),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3CD))
                    .padding(12.dp)
            )
        }

        CarAd(
            navController = navController,
            adds = carList,
            favCarsList = favCarsList,
            favCarsViewModel = favCarsViewModel,
            viewModel = viewModel,
            gridState = gridState,
            onLoadMoreClick = { viewModel.fetchNextPage() }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CarAd(
    navController: NavHostController,
    adds: List<CarSpecs>,
    favCarsList: List<FavouriteCar>,
    favCarsViewModel: FavouriteCarsViewModel,
    viewModel: MainViewModel,
    gridState: LazyGridState,
    onLoadMoreClick: (() -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(adds.filter { !it.id.isNullOrBlank() }) { item ->
            val itemId = item.id.orEmpty()
            val isFavCar = favCarsList.contains(FavouriteCar(itemId))
            AdItem(navController, item, itemId, isFavCar, favCarsViewModel, viewModel)
        }


        if (onLoadMoreClick != null) {
            item(span = { GridItemSpan(2) }) {
                Button(
                    onClick = onLoadMoreClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Load more")
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun getDaysSince(listingDate: String, soldDate: String?): Int {
    val startDate = parseCarDate(listingDate) ?: return 0
    val endDate = soldDate
        ?.takeIf { it.isNotBlank() }
        ?.let { parseCarDate(it) }
        ?: LocalDate.now()

    return ChronoUnit.DAYS.between(startDate, endDate).toInt()
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseCarDate(value: String): LocalDate? =
    runCatching { LocalDate.parse(value.take(10)) }
        .getOrElse {
            runCatching { LocalDateTime.parse(value).toLocalDate() }.getOrNull()
        }

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AdItem(
    navController: NavHostController, carSpecs: CarSpecs, carId: String,
    isFavCar: Boolean, favCarsViewModel: FavouriteCarsViewModel, viewModel: MainViewModel) {
    val favoriteScale by animateFloatAsState(
        targetValue = if (isFavCar) 1.1f else 1f,
        label = "favoriteScale"
    )
    val photoUrl = carSpecs.photoUrls.orEmpty().firstOrNull { it.isNotBlank() }
        ?: viewModel.getPhotoUrl(carSpecs.photoPath)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .height(360.dp)
            .animateContentSize()
            .clickable {
                navController.navigate(
                    Screen.CarDetailsScreen.withArgs(carId)
                )
            }
    ) {
        Column()
        {
            AsyncImage(
                model = photoUrl,
                contentDescription = "car photo",
                placeholder = painterResource(id = R.drawable.no_image),
                error = painterResource(id = R.drawable.no_image),
                onSuccess = {
                    Log.d("CarPhoto", "List image loaded: carId=$carId, url=$photoUrl")
                },
                onError = {
                    Log.e(
                        "CarPhoto",
                        "List image failed: carId=$carId, photoPath=${carSpecs.photoPath}, photoUrls=${carSpecs.photoUrls.orEmpty().size}, url=$photoUrl",
                        it.result.throwable
                    )
                },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.12f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            )

            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 10.dp, end = 2.dp, top = 2.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            color = if(carSpecs.disappearedAt.isNullOrBlank()) Color(0xFF4D8B57) else Color(0xFFC15B55)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${getDaysSince(carSpecs.postedAt, carSpecs.disappearedAt)} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    if (isFavCar) {
                        favCarsViewModel.deleteFavCar(carId)
                    }
                    else {
                        favCarsViewModel.addFavCar(carId)
                    }
                }) {
                    Icon(
                        painter = painterResource(if(isFavCar) R.drawable.favourite else R.drawable.favourite_border),
                        contentDescription = null,
                        tint = if (isFavCar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(23.dp)
                            .graphicsLayer(scaleX = favoriteScale, scaleY = favoriteScale)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${carSpecs.brand} ${carSpecs.model} (${carSpecs.year})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Column(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    SpecRow(icon = R.drawable.speed, text = "${carSpecs.mileage} km")
                    SpecRow(icon = R.drawable.gas_station, text = carSpecs.urbanConsumption.toDisplayValueOrDash())
                    SpecRow(icon = R.drawable.engine, text = "${carSpecs.enginePower} KM")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${carSpecs.price} PLN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecRow(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
