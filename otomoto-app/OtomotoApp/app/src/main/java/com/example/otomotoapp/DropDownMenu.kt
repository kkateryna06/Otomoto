package com.example.otomotoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme
import org.w3c.dom.Text

@Composable
fun DropDownMenu(carSpecs: CarSpecs, textMenu: String, modifier: Modifier = Modifier) {
    var isDropDownMenuExpanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp).padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = textMenu,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = { isDropDownMenuExpanded = !isDropDownMenuExpanded }) {
                Icon(
                    painter = if (isDropDownMenuExpanded) {
                        painterResource(id = R.drawable.arrow_up)
                    } else {
                        painterResource(id = R.drawable.arrow_down)
                    },
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        if (isDropDownMenuExpanded) {
            DropDownMenuContent(carSpecs = carSpecs, textMenu = textMenu)
        }
    }
}

@Composable
fun DropDownMenuContent(carSpecs: CarSpecs, textMenu: String) {
    if (textMenu == "Basic") {
        Column {
            DropDownMenuContentText(parameterName = "Color", parameterValue = carSpecs.color)
            DropDownMenuContentText(parameterName = "Number of doors", parameterValue = carSpecs.door_count.toString())
            DropDownMenuContentText(parameterName = "Number of seats", parameterValue = carSpecs.seats_count.toString())
            DropDownMenuContentText(parameterName = "Generation", parameterValue = carSpecs.generation)
        }
    }
    if (textMenu == "Specification") {
        Column {
            DropDownMenuContentText(parameterName = "Fuel type", parameterValue = carSpecs.fuel_type)
            DropDownMenuContentText(parameterName = "Engine capacity", parameterValue = carSpecs.engine_capacity.toString())
            DropDownMenuContentText(parameterName = "Engine power", parameterValue = carSpecs.engine_power.toString())
            DropDownMenuContentText(parameterName = "Body type", parameterValue = carSpecs.body_type)
            DropDownMenuContentText(parameterName = "Gearbox", parameterValue = carSpecs.gearbox)
            DropDownMenuContentText(parameterName = "Transmission", parameterValue = carSpecs.transmission)
            DropDownMenuContentText(parameterName = "Urban consumption", parameterValue = carSpecs.urban_consumption)
            DropDownMenuContentText(parameterName = "Extra urban consumption", parameterValue = carSpecs.extra_urban_consumption)
        }
    }
    if (textMenu == "Description") {
        Column {
            Text(text = carSpecs.description)
        }
    }
}

@Composable
fun DropDownMenuContentText(parameterName: String, parameterValue: String?) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 38.dp)
    ) {
        Text(text = parameterName)
        Text(text = parameterValue ?: "-")
    }
    Row(modifier = Modifier.fillMaxWidth().height(1.dp)
        .background(color = MaterialTheme.colorScheme.primaryContainer)) {  }
}

@Preview(showBackground = true)
@Composable
fun DropDownMenuPreview() {
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
        DropDownMenu(carSpecs = carSpecs, textMenu = "Basic")

    }
}