package com.example.otomotoapp.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import com.example.otomotoapp.data.CarSpecs
import com.example.otomotoapp.MainViewModel
import com.example.otomotoapp.R
import com.example.otomotoapp.Screen
import com.example.otomotoapp.database.FavouriteCar
import com.example.otomotoapp.database.FavouriteCarsViewModel
import com.example.otomotoapp.screen_elements.TopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun OtomotoMainScreen(viewModel: MainViewModel, favCarsViewModel: FavouriteCarsViewModel,
                      navController: NavHostController) {
    val carList by viewModel.carList.observeAsState(emptyList())
    val errorMessage by viewModel.errorMessage.observeAsState("")
    val isSpecialCarEnabled by viewModel.isSpecialCarEnabled.observeAsState(false)

    val favCarsList by favCarsViewModel.favouriteCars.collectAsState()
    Log.d("DEBUG", "$favCarsList")

    LaunchedEffect(Unit) {
        viewModel.fetchCars()
    }

    if (errorMessage?.isNotEmpty() == true) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text(text = errorMessage!!, color = Color.Red) }
    }
    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(isSpecialCarEnabled, viewModel, navController)
        CarAd(navController, carList, isSpecialCarEnabled, favCarsList, favCarsViewModel)
    }
}

@Composable
fun CarAd(
    navController: NavHostController, adds: List<CarSpecs>,
    isSpecialCarEnabled: Boolean, favCarsList: List<FavouriteCar>,
    favCarsViewModel: FavouriteCarsViewModel) {
    Row{
        LazyVerticalGrid(GridCells.Fixed(2)) {
            items(adds) {
                item ->
                val isFavCar = favCarsList.contains(FavouriteCar(item.car_id.toLong()))
                AdItem(navController, item, isSpecialCarEnabled, isFavCar, favCarsViewModel)
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
    isFavCar: Boolean, favCarsViewModel: FavouriteCarsViewModel) {

    Card(
        modifier = Modifier
            .height(400.dp)
            .padding(8.dp)
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

//            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
//                Text(text = carSpecs.date, style = TextStyle(fontSize = 7.sp))
//                Text(text = carSpecs.sell_date, style = TextStyle(fontSize = 7.sp))
//            }
            Image(
                painter = painterResource(id = R.drawable.no_image),
                contentDescription = "car photo"
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 5.dp, vertical = 10.dp)
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



@Preview(showBackground = true)
@Composable
fun OtomotoMainScreenPreview() {
    AppTheme(dynamicColor = false) {
        val fakeViewModel = MainViewModel()
        val navController = rememberNavController()
        var isFilterMenuExpanded = remember { mutableStateOf(false) }

        val carList = listOf(
            CarSpecs(
                car_id = "6132407608",
                date = "2025-01-05 10:58:16",
                sell_date = "2025-17-05 10:58:16",
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
                description = "Honda Civic VIII Rok produkcji: 2006 Przebieg: 214386 km Bezwypadkowy Pochodzenie: samochód kupiony w Niemczech od pierwszego właściciela, pierwszy właściciel w Polsce Samochód z udokumentowaną historią serwisową Wyposażenie (wybrane elementy): - Silnik: 2.2 i-CDTi (140 KM, 340 Nm) - Skrzynia Manualna 6 biegowa - Napęd na przednią oś - Rozrząd na łańcuchu - koła aluminiowe 17-calowe oryginalne z salonu - Lakier czarny perłowy - Tapicerka materiałowa - Wykończenie wnętrza plastik + aluminium - Fotele z możliwością regulacji - Kanapa z dostępem do przestrzeni załadunkowej i podłokietnikiem - Kierownica multimedialna obszyta skórą - Klimatyzacja automatyczna jednostrefowa - Światła przeciwmgłowe przednie i tylne - Tempomat - Czujnik zmierzchu - Czujnik deszczu - Elektrycznie regulowane lusterka - Elektryczne szyby przednie i tylne - system Isofix - wentylowany schowek - Radio na płytę - System Honda komputera pokładowego - lusterka boczne składane + podgrzewane - System ściemniania ekranu podczas nocnej jazdy - Tylne czujniki parkowania Samochód osobiście przywiozłem od pierwszego właściciela w Niemczech w roku 2018 i jestem pierwszym właścicielem w Polsce. Auto było przeze mnie użytkowane od 2018 roku do chwili obecnej. Podczas zakupu samochodu przebieg wynosił 167 tysięcy , na chwilę obecną przebieg samochodu to 214 tysięcy. Obecnie auto posiada na sobie opony z roku 2022 z dużą ilością bieżnika, auto jest ubezpieczone do roku 2025 do października. Przegląd Techniczny robiony był w listopadzie 2024. Stan samochodu uważam na bardzo dobry bez wkładu finansowego. * Regularnie wymieniałem olej 5w-30 (1 raz w roku max do 10 tysięcy km) * Co roku wymieniane były filtr (olejowy, kabinowy, paliwa, powietrza) * Olej w skrzyni biegów wymieniałem 2 razy podczas swojego użytkowania * Klocki hamulcowe zmieniane były 2 razy * Tarcze hamulcowe zmieniane były 2 razy * Akumulator wymieniony został w roku 2023 * Samochód posiada wykupione ubezpieczenie OC do 10.2025 * Samochód posiada aktualny przegląd techniczny do 09.2025 W cenie zawarte jest: * 4 sztuki opon letnich Viking, zakupionych przeze mnie w roku 2024 * Koło dojazdowe * Transmiter do puszczania muzyki z telefonu Powodem sprzedaży jest zmiana samochodu na auto dostawcze. Na prośbę kupującego istnieje możliwość sprawdzenia stanu technicznego samochodu w dowolnie wybranym serwisie na terenie Świnoujścia Lokalizacja: Zachodniopomorskie, Świnoujście Szczegóły udzielam telefoniczne pod nr tel.  Marcel Kopaczewski.",
                link = "https://www.otomoto.pl/osobowe/oferta/honda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
                photo_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
                html_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html"
            ),
            CarSpecs(
                car_id = "613240708",
                date = "2025-01-05 10:58:16",
                sell_date = "2025-17-05 10:58:16",
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
                description = "Honda Civic VIII Rok produkcji: 2006 Przebieg: 214386 km Bezwypadkowy Pochodzenie: samochód kupiony w Niemczech od pierwszego właściciela, pierwszy właściciel w Polsce Samochód z udokumentowaną historią serwisową Wyposażenie (wybrane elementy): - Silnik: 2.2 i-CDTi (140 KM, 340 Nm) - Skrzynia Manualna 6 biegowa - Napęd na przednią oś - Rozrząd na łańcuchu - koła aluminiowe 17-calowe oryginalne z salonu - Lakier czarny perłowy - Tapicerka materiałowa - Wykończenie wnętrza plastik + aluminium - Fotele z możliwością regulacji - Kanapa z dostępem do przestrzeni załadunkowej i podłokietnikiem - Kierownica multimedialna obszyta skórą - Klimatyzacja automatyczna jednostrefowa - Światła przeciwmgłowe przednie i tylne - Tempomat - Czujnik zmierzchu - Czujnik deszczu - Elektrycznie regulowane lusterka - Elektryczne szyby przednie i tylne - system Isofix - wentylowany schowek - Radio na płytę - System Honda komputera pokładowego - lusterka boczne składane + podgrzewane - System ściemniania ekranu podczas nocnej jazdy - Tylne czujniki parkowania Samochód osobiście przywiozłem od pierwszego właściciela w Niemczech w roku 2018 i jestem pierwszym właścicielem w Polsce. Auto było przeze mnie użytkowane od 2018 roku do chwili obecnej. Podczas zakupu samochodu przebieg wynosił 167 tysięcy , na chwilę obecną przebieg samochodu to 214 tysięcy. Obecnie auto posiada na sobie opony z roku 2022 z dużą ilością bieżnika, auto jest ubezpieczone do roku 2025 do października. Przegląd Techniczny robiony był w listopadzie 2024. Stan samochodu uważam na bardzo dobry bez wkładu finansowego. * Regularnie wymieniałem olej 5w-30 (1 raz w roku max do 10 tysięcy km) * Co roku wymieniane były filtr (olejowy, kabinowy, paliwa, powietrza) * Olej w skrzyni biegów wymieniałem 2 razy podczas swojego użytkowania * Klocki hamulcowe zmieniane były 2 razy * Tarcze hamulcowe zmieniane były 2 razy * Akumulator wymieniony został w roku 2023 * Samochód posiada wykupione ubezpieczenie OC do 10.2025 * Samochód posiada aktualny przegląd techniczny do 09.2025 W cenie zawarte jest: * 4 sztuki opon letnich Viking, zakupionych przeze mnie w roku 2024 * Koło dojazdowe * Transmiter do puszczania muzyki z telefonu Powodem sprzedaży jest zmiana samochodu na auto dostawcze. Na prośbę kupującego istnieje możliwość sprawdzenia stanu technicznego samochodu w dowolnie wybranym serwisie na terenie Świnoujścia Lokalizacja: Zachodniopomorskie, Świnoujście Szczegóły udzielam telefoniczne pod nr tel.  Marcel Kopaczewski.",
                link = "https://www.otomoto.pl/osobowe/oferta/honda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
                photo_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
                html_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html"
            ),
            CarSpecs(
                car_id = "6132407608",
                date = "2025-01-05 10:58:16",
                sell_date = "2025-17-05 10:58:16",
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
                description = "Honda Civic VIII Rok produkcji: 2006 Przebieg: 214386 km Bezwypadkowy Pochodzenie: samochód kupiony w Niemczech od pierwszego właściciela, pierwszy właściciel w Polsce Samochód z udokumentowaną historią serwisową Wyposażenie (wybrane elementy): - Silnik: 2.2 i-CDTi (140 KM, 340 Nm) - Skrzynia Manualna 6 biegowa - Napęd na przednią oś - Rozrząd na łańcuchu - koła aluminiowe 17-calowe oryginalne z salonu - Lakier czarny perłowy - Tapicerka materiałowa - Wykończenie wnętrza plastik + aluminium - Fotele z możliwością regulacji - Kanapa z dostępem do przestrzeni załadunkowej i podłokietnikiem - Kierownica multimedialna obszyta skórą - Klimatyzacja automatyczna jednostrefowa - Światła przeciwmgłowe przednie i tylne - Tempomat - Czujnik zmierzchu - Czujnik deszczu - Elektrycznie regulowane lusterka - Elektryczne szyby przednie i tylne - system Isofix - wentylowany schowek - Radio na płytę - System Honda komputera pokładowego - lusterka boczne składane + podgrzewane - System ściemniania ekranu podczas nocnej jazdy - Tylne czujniki parkowania Samochód osobiście przywiozłem od pierwszego właściciela w Niemczech w roku 2018 i jestem pierwszym właścicielem w Polsce. Auto było przeze mnie użytkowane od 2018 roku do chwili obecnej. Podczas zakupu samochodu przebieg wynosił 167 tysięcy , na chwilę obecną przebieg samochodu to 214 tysięcy. Obecnie auto posiada na sobie opony z roku 2022 z dużą ilością bieżnika, auto jest ubezpieczone do roku 2025 do października. Przegląd Techniczny robiony był w listopadzie 2024. Stan samochodu uważam na bardzo dobry bez wkładu finansowego. * Regularnie wymieniałem olej 5w-30 (1 raz w roku max do 10 tysięcy km) * Co roku wymieniane były filtr (olejowy, kabinowy, paliwa, powietrza) * Olej w skrzyni biegów wymieniałem 2 razy podczas swojego użytkowania * Klocki hamulcowe zmieniane były 2 razy * Tarcze hamulcowe zmieniane były 2 razy * Akumulator wymieniony został w roku 2023 * Samochód posiada wykupione ubezpieczenie OC do 10.2025 * Samochód posiada aktualny przegląd techniczny do 09.2025 W cenie zawarte jest: * 4 sztuki opon letnich Viking, zakupionych przeze mnie w roku 2024 * Koło dojazdowe * Transmiter do puszczania muzyki z telefonu Powodem sprzedaży jest zmiana samochodu na auto dostawcze. Na prośbę kupującego istnieje możliwość sprawdzenia stanu technicznego samochodu w dowolnie wybranym serwisie na terenie Świnoujścia Lokalizacja: Zachodniopomorskie, Świnoujście Szczegóły udzielam telefoniczne pod nr tel.  Marcel Kopaczewski.",
                link = "https://www.otomoto.pl/osobowe/oferta/honda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
                photo_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
                html_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html"
            ),
            CarSpecs(
                car_id = "613247608",
                date = "2025-01-05 10:58:16",
                sell_date = "2025-17-05 10:58:16",
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
                description = "Honda Civic VIII Rok produkcji: 2006 Przebieg: 214386 km Bezwypadkowy Pochodzenie: samochód kupiony w Niemczech od pierwszego właściciela, pierwszy właściciel w Polsce Samochód z udokumentowaną historią serwisową Wyposażenie (wybrane elementy): - Silnik: 2.2 i-CDTi (140 KM, 340 Nm) - Skrzynia Manualna 6 biegowa - Napęd na przednią oś - Rozrząd na łańcuchu - koła aluminiowe 17-calowe oryginalne z salonu - Lakier czarny perłowy - Tapicerka materiałowa - Wykończenie wnętrza plastik + aluminium - Fotele z możliwością regulacji - Kanapa z dostępem do przestrzeni załadunkowej i podłokietnikiem - Kierownica multimedialna obszyta skórą - Klimatyzacja automatyczna jednostrefowa - Światła przeciwmgłowe przednie i tylne - Tempomat - Czujnik zmierzchu - Czujnik deszczu - Elektrycznie regulowane lusterka - Elektryczne szyby przednie i tylne - system Isofix - wentylowany schowek - Radio na płytę - System Honda komputera pokładowego - lusterka boczne składane + podgrzewane - System ściemniania ekranu podczas nocnej jazdy - Tylne czujniki parkowania Samochód osobiście przywiozłem od pierwszego właściciela w Niemczech w roku 2018 i jestem pierwszym właścicielem w Polsce. Auto było przeze mnie użytkowane od 2018 roku do chwili obecnej. Podczas zakupu samochodu przebieg wynosił 167 tysięcy , na chwilę obecną przebieg samochodu to 214 tysięcy. Obecnie auto posiada na sobie opony z roku 2022 z dużą ilością bieżnika, auto jest ubezpieczone do roku 2025 do października. Przegląd Techniczny robiony był w listopadzie 2024. Stan samochodu uważam na bardzo dobry bez wkładu finansowego. * Regularnie wymieniałem olej 5w-30 (1 raz w roku max do 10 tysięcy km) * Co roku wymieniane były filtr (olejowy, kabinowy, paliwa, powietrza) * Olej w skrzyni biegów wymieniałem 2 razy podczas swojego użytkowania * Klocki hamulcowe zmieniane były 2 razy * Tarcze hamulcowe zmieniane były 2 razy * Akumulator wymieniony został w roku 2023 * Samochód posiada wykupione ubezpieczenie OC do 10.2025 * Samochód posiada aktualny przegląd techniczny do 09.2025 W cenie zawarte jest: * 4 sztuki opon letnich Viking, zakupionych przeze mnie w roku 2024 * Koło dojazdowe * Transmiter do puszczania muzyki z telefonu Powodem sprzedaży jest zmiana samochodu na auto dostawcze. Na prośbę kupującego istnieje możliwość sprawdzenia stanu technicznego samochodu w dowolnie wybranym serwisie na terenie Świnoujścia Lokalizacja: Zachodniopomorskie, Świnoujście Szczegóły udzielam telefoniczne pod nr tel.  Marcel Kopaczewski.",
                link = "https://www.otomoto.pl/osobowe/oferta/honda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
                photo_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
                html_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html"
            )
        )


        Column(modifier = Modifier.fillMaxSize()) {

//            TopAppBar(false, fakeViewModel)
//            CarAd(navController, carList, false, mutableStateOf(listOf(FavouriteCar(613247608L))), viewModel())
        }
    }
}