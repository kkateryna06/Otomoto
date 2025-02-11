package com.example.otomotoapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun OtomotoMainScreen(viewModel: MainViewModel, navController: NavHostController) {
    val carList by viewModel.carList.observeAsState(emptyList())
    val errorMessage by viewModel.errorMessage.observeAsState("")
    val isSpecialCarEnabled by viewModel.isSpecialCarEnabled.observeAsState(false)

    LaunchedEffect(Unit) {
        viewModel.fetchCars()
    }

    if (errorMessage?.isNotEmpty() == true) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text(text = errorMessage!!, color = Color.Red) }
    }
    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(isSpecialCarEnabled, viewModel)
        SearchField()
        CarAd(navController, carList, isSpecialCarEnabled)
    }
}

@Composable
fun CarAd(navController: NavHostController, adds: List<CarSpecs>, isSpecialCarEnabled: Boolean) {
    Row{
        LazyVerticalGrid(GridCells.Fixed(2)) {
            items(adds) {
                item ->
                AdItem(navController, item, isSpecialCarEnabled)
            }
        }
    }
}

@Composable
fun AdItem(navController: NavHostController, carSpecs: CarSpecs, isSpecialCarEnabled: Boolean) {
    Card(
        modifier = Modifier
            .height(370.dp)
            .padding(8.dp)
            .clickable {
                navController.navigate(Screen.CarDetailsScreen.withArgs(carSpecs.car_id, isSpecialCarEnabled))
            }
    ) {
        Column()
        {
            Image(
                painter = painterResource(id = R.drawable.no_image),
                contentDescription = "car photo"
            )
            Column(
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp).fillMaxHeight(),
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
                            modifier = Modifier.size(25.dp).padding(start = 2.dp)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
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