package com.example.otomotoapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import com.example.compose.backgroundDark

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
        CarAd(navController, carList)
    }
}

@Composable
fun TopAppBar(isSpecialCarEnabled: Boolean, viewModel: MainViewModel){
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(color = MaterialTheme.colorScheme.primaryContainer)
        .padding(horizontal = 19.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,

    ) {
        IconButton(
            modifier = Modifier.size(30.dp),
            onClick = {}) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", modifier = Modifier.fillMaxSize())
        }

        Text(text = "Otomoto Cars", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Switch(
                modifier = Modifier.size(30.dp),
                checked = isSpecialCarEnabled,
                onCheckedChange = { isChecked -> viewModel.toggleSpecialCarSwitch(isChecked) })
            Text(
                text= "Special"
            )
        }
    }
}

@Composable
fun SearchField() {
    var searchText by remember { mutableStateOf(TextFieldValue("Search")) }

    Box(modifier = Modifier.padding(15.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(25.dp))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 17.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.filter), contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = null,
                singleLine = true,
                modifier = Modifier.height(55.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                )
            )

            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.search), contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

        }
    }
}

@Composable
fun CarAd(navController: NavHostController, adds: List<CarSpecs>) {
    Row{
        LazyVerticalGrid(GridCells.Fixed(2)) {
            items(adds) {
                item ->
                AdItem(navController, item)
            }
        }
    }
}

@Composable
fun AdItem(navController: NavHostController, carSpecs: CarSpecs) {
    Card(
        modifier = Modifier
            .height(370.dp)
            .padding(8.dp)
            .clickable {
                navController.navigate(Screen.CarDetailsScreen.withArgs(carSpecs.car_id))
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


@Preview(showBackground = true)
@Composable
fun OtomotoMainScreenPreview() {
    val fakeCars = listOf(
        CarSpecs(
            car_id = "6132407608",
            date = "2025-01-05 10:58:16",
            mark = "Honda",
            model = "Civic",
            version = "2.2i-CTDi DPF Sport",
            year = 2006,
            mileage = 214386,
            fuel_type = "Diesel",
            engine_capacity = 2204,
            engine_power = 140,
            price = 17800,
            body_type = "Kompakt",
            gearbox = "Manualna",
            transmission = "Na przednie koła",
            urban_consumption = "6.7 l/100km",
            extra_urban_consumption = "4.5 l/100km",
            color = "Czarny",
            door_count = 5,
            seats_count = 5,
            generation = "VIII (2006-2011)",
            has_registration = true,
            seller_type = "PRIVATE",
            description = "Honda Civic VIII...",
            link = "https://www.otomoto.pl/osobowe/oferta/honda-civic...",
            photo_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos...",
            html_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls..."
        ),
        CarSpecs(
            car_id = "6132407608",
            date = "2025-01-05 10:58:16",
            mark = "Honda",
            model = "Civic",
            version = "2.2i-CTDi DPF Sport",
            year = 2006,
            mileage = 214386,
            fuel_type = "Diesel",
            engine_capacity = 2204,
            engine_power = 140,
            price = 17800,
            body_type = "Kompakt",
            gearbox = "Manualna",
            transmission = "Na przednie koła",
            urban_consumption = "6.7 l/100km",
            extra_urban_consumption = "4.5 l/100km",
            color = "Czarny",
            door_count = 5,
            seats_count = 5,
            generation = "VIII (2006-2011)",
            has_registration = true,
            seller_type = "PRIVATE",
            description = "Honda Civic VIII...",
            link = "https://www.otomoto.pl/osobowe/oferta/honda-civic...",
            photo_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos...",
            html_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls..."
        ),
        CarSpecs(
            car_id = "6132407608",
            date = "2025-01-05 10:58:16",
            mark = "Honda",
            model = "Civic",
            version = "2.2i-CTDi DPF Sport",
            year = 2006,
            mileage = 214386,
            fuel_type = "Diesel",
            engine_capacity = 2204,
            engine_power = 140,
            price = 17800,
            body_type = "Kompakt",
            gearbox = "Manualna",
            transmission = "Na przednie koła",
            urban_consumption = "6.7 l/100km",
            extra_urban_consumption = "4.5 l/100km",
            color = "Czarny",
            door_count = 5,
            seats_count = 5,
            generation = "VIII (2006-2011)",
            has_registration = true,
            seller_type = "PRIVATE",
            description = "Honda Civic VIII...",
            link = "https://www.otomoto.pl/osobowe/oferta/honda-civic...",
            photo_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos...",
            html_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls..."
        ),
        CarSpecs(
            car_id = "6132407608",
            date = "2025-01-05 10:58:16",
            mark = "Honda",
            model = "Civic",
            version = "2.2i-CTDi DPF Sport",
            year = 2006,
            mileage = 214386,
            fuel_type = "Diesel",
            engine_capacity = 2204,
            engine_power = 140,
            price = 17800,
            body_type = "Kompakt",
            gearbox = "Manualna",
            transmission = "Na przednie koła",
            urban_consumption = "6.7 l/100km",
            extra_urban_consumption = "4.5 l/100km",
            color = "Czarny",
            door_count = 5,
            seats_count = 5,
            generation = "VIII (2006-2011)",
            has_registration = true,
            seller_type = "PRIVATE",
            description = "Honda Civic VIII...",
            link = "https://www.otomoto.pl/osobowe/oferta/honda-civic...",
            photo_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos...",
            html_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls..."
        ),
        CarSpecs(
            car_id = "6132407608",
            date = "2025-01-05 10:58:16",
            mark = "Honda",
            model = "Civic",
            version = "2.2i-CTDi DPF Sport",
            year = 2006,
            mileage = 214386,
            fuel_type = "Diesel",
            engine_capacity = 2204,
            engine_power = 140,
            price = 17800,
            body_type = "Kompakt",
            gearbox = "Manualna",
            transmission = "Na przednie koła",
            urban_consumption = "6.7 l/100km",
            extra_urban_consumption = "4.5 l/100km",
            color = "Czarny",
            door_count = 5,
            seats_count = 5,
            generation = "VIII (2006-2011)",
            has_registration = true,
            seller_type = "PRIVATE",
            description = "Honda Civic VIII...",
            link = "https://www.otomoto.pl/osobowe/oferta/honda-civic...",
            photo_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos...",
            html_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls..."
        ),
        CarSpecs(
            car_id = "6132407608",
            date = "2025-01-05 10:58:16",
            mark = "Honda",
            model = "Civic",
            version = "2.2i-CTDi DPF Sport",
            year = 2006,
            mileage = 214386,
            fuel_type = "Diesel",
            engine_capacity = 2204,
            engine_power = 140,
            price = 17800,
            body_type = "Kompakt",
            gearbox = "Manualna",
            transmission = "Na przednie koła",
            urban_consumption = "6.7 l/100km",
            extra_urban_consumption = "4.5 l/100km",
            color = "Czarny",
            door_count = 5,
            seats_count = 5,
            generation = "VIII (2006-2011)",
            has_registration = true,
            seller_type = "PRIVATE",
            description = "Honda Civic VIII...",
            link = "https://www.otomoto.pl/osobowe/oferta/honda-civic...",
            photo_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos...",
            html_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls..."
        ),

    )

    val fakeViewModel = object : MainViewModel() {}

    val navController = rememberNavController()

    AppTheme(dynamicColor = false) {
        Column(modifier = Modifier.fillMaxSize()) {

            TopAppBar(false, fakeViewModel)
            SearchField()
            CarAd(navController, fakeCars)
        }
    }
}



