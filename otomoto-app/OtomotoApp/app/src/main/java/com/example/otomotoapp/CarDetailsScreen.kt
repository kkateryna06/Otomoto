package com.example.otomotoapp

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.compose.AppTheme

@Composable
fun CarDetailsScreen(carId: String, viewModel: MainViewModel, isSpecialCarEnabled: Boolean, navController: NavHostController) {
    val carSpecs by viewModel.getCarById(carId).observeAsState()
    val carPhotos by viewModel.carPhotosLiveData.observeAsState()
    val carPhoto = carPhotos?.get(carId)

    LaunchedEffect(carId) {
        viewModel.getPhotoById(carId)
    }
    println(carPhoto)

    Box(modifier = Modifier.fillMaxSize()) {
        Column() {
            TopAppBar(isSpecialCarEnabled = isSpecialCarEnabled, viewModel = viewModel, navController = navController)
            if (carSpecs != null) {
                CarDetails(carSpecs = carSpecs!!, carPhoto)
            }
        }
        if (carSpecs != null) {
            BottomAppBar(carSpecs!!.price, carSpecs!!.link, Modifier.align(Alignment.BottomCenter))
        } else {
            BottomAppBar(0, "")
        }
    }
}

@Composable
fun CarDetails(carSpecs: CarSpecs, carPhoto: Bitmap?) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
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
        Text(
            text = "${carSpecs.mark} ${carSpecs.model} ${carSpecs.version ?: ""} (${carSpecs.year})",
            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        DropDownMenu(carSpecs = carSpecs, textMenu = "Basic", isDropDownMenuExpanded = true)
        DropDownMenu(carSpecs = carSpecs, textMenu = "Specification", isDropDownMenuExpanded = true)


        DropDownMenu(carSpecs = carSpecs, textMenu = "Description")
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun BottomAppBar(carPrice: Int, carLink: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
        Button(onClick = {}) { 
            Text(text = "View in Otomoto")
        }
        Text(text = "$carPrice PLN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
fun CarDetailsScreenPreview() {
    val carSpecs = CarSpecs(
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
        description = "Honda Civic VIII Rok produkcji: 2006 Przebieg: 214386 km Bezwypadkowy Pochodzenie: samochód kupiony w Niemczech od pierwszego właściciela, pierwszy właściciel w Polsce Samochód z udokumentowaną historią serwisową Wyposażenie (wybrane elementy): - Silnik: 2.2 i-CDTi (140 KM, 340 Nm) - Skrzynia Manualna 6 biegowa - Napęd na przednią oś - Rozrząd na łańcuchu - koła aluminiowe 17-calowe oryginalne z salonu - Lakier czarny perłowy - Tapicerka materiałowa - Wykończenie wnętrza plastik + aluminium - Fotele z możliwością regulacji - Kanapa z dostępem do przestrzeni załadunkowej i podłokietnikiem - Kierownica multimedialna obszyta skórą - Klimatyzacja automatyczna jednostrefowa - Światła przeciwmgłowe przednie i tylne - Tempomat - Czujnik zmierzchu - Czujnik deszczu - Elektrycznie regulowane lusterka - Elektryczne szyby przednie i tylne - system Isofix - wentylowany schowek - Radio na płytę - System Honda komputera pokładowego - lusterka boczne składane + podgrzewane - System ściemniania ekranu podczas nocnej jazdy - Tylne czujniki parkowania Samochód osobiście przywiozłem od pierwszego właściciela w Niemczech w roku 2018 i jestem pierwszym właścicielem w Polsce. Auto było przeze mnie użytkowane od 2018 roku do chwili obecnej. Podczas zakupu samochodu przebieg wynosił 167 tysięcy , na chwilę obecną przebieg samochodu to 214 tysięcy. Obecnie auto posiada na sobie opony z roku 2022 z dużą ilością bieżnika, auto jest ubezpieczone do roku 2025 do października. Przegląd Techniczny robiony był w listopadzie 2024. Stan samochodu uważam na bardzo dobry bez wkładu finansowego. * Regularnie wymieniałem olej 5w-30 (1 raz w roku max do 10 tysięcy km) * Co roku wymieniane były filtr (olejowy, kabinowy, paliwa, powietrza) * Olej w skrzyni biegów wymieniałem 2 razy podczas swojego użytkowania * Klocki hamulcowe zmieniane były 2 razy * Tarcze hamulcowe zmieniane były 2 razy * Akumulator wymieniony został w roku 2023 * Samochód posiada wykupione ubezpieczenie OC do 10.2025 * Samochód posiada aktualny przegląd techniczny do 09.2025 W cenie zawarte jest: * 4 sztuki opon letnich Viking, zakupionych przeze mnie w roku 2024 * Koło dojazdowe * Transmiter do puszczania muzyki z telefonu Powodem sprzedaży jest zmiana samochodu na auto dostawcze. Na prośbę kupującego istnieje możliwość sprawdzenia stanu technicznego samochodu w dowolnie wybranym serwisie na terenie Świnoujścia Lokalizacja: Zachodniopomorskie, Świnoujście Szczegóły udzielam telefoniczne pod nr tel.  Marcel Kopaczewski.",
        link = "https://www.otomoto.pl/osobowe/oferta/honda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
        photo_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
        html_path = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html"
    )

    AppTheme(dynamicColor = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column() {
//                TopAppBar(isSpecialCarEnabled = false, viewModel = MainViewModel())
                //CarDetails(carSpecs = carSpecs)
            }
            BottomAppBar(carSpecs.price, carSpecs.link, Modifier.align(Alignment.BottomCenter))
        }
    }
}