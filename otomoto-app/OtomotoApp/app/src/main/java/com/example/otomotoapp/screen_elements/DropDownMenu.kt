package com.example.otomotoapp.screen_elements

import androidx.compose.animation.animateContentSize
import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.compose.AppTheme
import com.example.otomotoapp.data.CarSpecs
import com.example.otomotoapp.R
import com.example.otomotoapp.data.Location
import com.example.otomotoapp.ui.toDisplayValueOrDash
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.max

@Composable
fun DropDownMenu(carSpecs: CarSpecs, textMenu: String, isDropDownMenuExpanded: Boolean = false, modifier: Modifier = Modifier) {
    var isDropDownMenuExpanded by remember { mutableStateOf(isDropDownMenuExpanded) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
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
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            DropDownMenuContentText(parameterName = "Number of doors", parameterValue = carSpecs.doorCount.toString(), formatValue = false)
            DropDownMenuContentText(parameterName = "Number of seats", parameterValue = carSpecs.seats?.toString(), formatValue = false)
            DropDownMenuContentText(parameterName = "Generation", parameterValue = carSpecs.generation, formatValue = false)
        }
    }
    if (textMenu == "Specification") {
        Column {
            DropDownMenuContentText(parameterName = "Fuel type", parameterValue = carSpecs.fuelType)
            DropDownMenuContentText(parameterName = "Engine capacity", parameterValue = carSpecs.engineCapacity.toString(), formatValue = false)
            DropDownMenuContentText(parameterName = "Engine power", parameterValue = carSpecs.enginePower.toString(), formatValue = false)
            DropDownMenuContentText(parameterName = "Body type", parameterValue = carSpecs.bodyType)
            DropDownMenuContentText(parameterName = "Gearbox", parameterValue = carSpecs.gearbox)
            DropDownMenuContentText(parameterName = "Transmission", parameterValue = carSpecs.transmission)
            DropDownMenuContentText(parameterName = "Urban consumption", parameterValue = carSpecs.urbanConsumption)
            DropDownMenuContentText(parameterName = "Extra urban consumption", parameterValue = carSpecs.extraUrbanConsumption)
        }
    }
    if (textMenu == "Description") {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Text(
                text = carSpecs.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    if (textMenu == "Price history") {
        PriceHistoryContent(carSpecs)
    }
    if (textMenu == "Location") {
        val location = carSpecs.location
        if (location != null) {
            CarLocation(location)
        } else {
            DropDownMenuContentText(parameterName = "Location", parameterValue = null)
        }
    }
}

@Composable
fun DropDownMenuContentText(parameterName: String, parameterValue: String?, formatValue: Boolean = true) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = parameterName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (formatValue) parameterValue.toDisplayValueOrDash() else parameterValue ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
fun PriceHistoryContent(carSpecs: CarSpecs) {
    val points = carSpecs.priceHistory
        ?.entries
        ?.mapNotNull { entry ->
            parsePriceHistoryDate(entry.key)?.let { date ->
                PricePoint(date = date, price = entry.value)
            }
        }
        ?.sortedBy { it.date }
        .orEmpty()

    val currentPrice = points.lastOrNull()?.price ?: carSpecs.price
    val hasPriceChanges = points.map { it.price }.distinct().size > 1

    if (!hasPriceChanges) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                text = "Current price",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (currentPrice > 0) formatPrice(currentPrice) else "-",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "No price changes recorded",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        return
    }

    val first = points.first()
    val last = points.last()
    val minPoint = points.minBy { it.price }
    val maxPoint = points.maxBy { it.price }
    val priceDiff = last.price - first.price
    val diffText = when {
        priceDiff > 0 -> "+${formatPrice(priceDiff)}"
        priceDiff < 0 -> "-${formatPrice(-priceDiff)}"
        else -> "0 PLN"
    }

    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Current price",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatPrice(last.price),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = diffText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = when {
                    priceDiff < 0 -> Color(0xFF2E7D32)
                    priceDiff > 0 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .padding(top = 14.dp, bottom = 8.dp)
        ) {
            PriceHistoryChart(points = points)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatChartDate(first.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatChartDate(last.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropDownMenuContentText(parameterName = "Lowest price", parameterValue = "${formatPrice(minPoint.price)} (${formatChartDate(minPoint.date)})", formatValue = false)
        DropDownMenuContentText(parameterName = "Highest price", parameterValue = "${formatPrice(maxPoint.price)} (${formatChartDate(maxPoint.date)})", formatValue = false)
    }
}

@Composable
private fun PriceHistoryChart(points: List<PricePoint>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    val pointColor = MaterialTheme.colorScheme.surface
    val minPrice = points.minOf { it.price }
    val maxPrice = points.maxOf { it.price }
    val priceRange = max(1, maxPrice - minPrice)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val horizontalPadding = 10.dp.toPx()
        val verticalPadding = 12.dp.toPx()
        val chartWidth = size.width - horizontalPadding * 2
        val chartHeight = size.height - verticalPadding * 2

        repeat(4) { index ->
            val y = verticalPadding + chartHeight * index / 3f
            drawLine(
                color = gridColor,
                start = Offset(horizontalPadding, y),
                end = Offset(size.width - horizontalPadding, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val offsets = points.mapIndexed { index, point ->
            val x = if (points.size == 1) {
                horizontalPadding + chartWidth / 2f
            } else {
                horizontalPadding + chartWidth * index / (points.lastIndex.toFloat())
            }
            val normalized = (point.price - minPrice).toFloat() / priceRange
            val y = verticalPadding + chartHeight - chartHeight * normalized
            Offset(x, y)
        }

        if (offsets.size == 1) {
            drawCircle(color = lineColor, radius = 5.dp.toPx(), center = offsets.first())
            return@Canvas
        }

        val linePath = Path().apply {
            moveTo(offsets.first().x, offsets.first().y)
            offsets.drop(1).forEach { lineTo(it.x, it.y) }
        }
        val fillPath = Path().apply {
            moveTo(offsets.first().x, size.height - verticalPadding)
            offsets.forEach { lineTo(it.x, it.y) }
            lineTo(offsets.last().x, size.height - verticalPadding)
            close()
        }

        drawPath(path = fillPath, color = fillColor)
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        offsets.forEach {
            drawCircle(color = pointColor, radius = 5.dp.toPx(), center = it)
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = it)
        }
    }
}

private data class PricePoint(
    val date: LocalDate,
    val price: Int
)

private fun parsePriceHistoryDate(value: String): LocalDate? =
    runCatching { LocalDate.parse(value.take(10)) }
        .getOrElse {
            runCatching { LocalDateTime.parse(value).toLocalDate() }.getOrNull()
        }

private fun formatChartDate(date: LocalDate): String =
    "${date.dayOfMonth.toString().padStart(2, '0')}.${date.monthValue.toString().padStart(2, '0')}.${date.year}"

private fun formatPrice(price: Int): String =
    "%,d PLN".format(price).replace(',', ' ')

@Composable
fun CarLocation(location: Location) {
    val latlngPosition = LatLng(location.latitude, location.longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latlngPosition, location.zoom.toFloat())
    }
    val context = LocalContext.current
//
    Column(modifier = Modifier.padding(top = 10.dp)) {
        Text(
            getAddressFromLatLng(context, latlngPosition),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
        GoogleMap(
            onMapClick = {},
            cameraPositionState = cameraPositionState,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Marker(state = MarkerState(
                position = latlngPosition
            )
            )
            Circle(
                center = latlngPosition,
                radius = location.radius.toDouble(),
                fillColor = Color.Gray.copy(alpha = 0.2f),
                strokeColor = Color.Transparent
            )
        }
    }
}

fun getAddressFromLatLng(context: Context, position: LatLng): String {
    val address = Geocoder(context)
        .getFromLocation(position.latitude, position.longitude,1 )?.firstOrNull()
    return if (address?.locality != null) {
        "${address.locality}, ${address.countryName}"
    }
    else if (address?.adminArea != null) {
        "${address.adminArea}, ${address.countryName}"
    }
    else {
        "Invalid location"
    }
}

@Preview(showBackground = true)
@Composable
fun DropDownMenuPreview() {
    val carSpecs = CarSpecs(
        id = "6132407608",
        postedAt = "2025-01-05 10:58:16",
        disappearedAt = "2025-17-05 10:58:16",
        brand = "Honda",
        model = "Civic",
        version = "2.2i-CTDi DPF Sport",
        year = 2006,
        mileage = 214386,
        fuelType = "Diesel",
        engineCapacity = 2204,
        enginePower = 140,
        priceHistory = mapOf("2025-01-05T10:58:16Z" to 17800),
        bodyType = "Kompakt",
        gearbox = "Manualna",
        transmission = "Na przednie koła",
        urbanConsumption = "6.7 l/100km",
        extraUrbanConsumption = "4.5 l/100km",
        color = "Czarny",
        doorCount = 5,
        seats = 5,
        generation = "VIII (2006-2011)",
        hasRegistration = true,
        sellerType = "PRIVATE",
        description = "Honda Civic VIII Rok produkcji: 2006 Przebieg: 214386 km Bezwypadkowy Pochodzenie: samochód kupiony w Niemczech od pierwszego właściciela, pierwszy właściciel w Polsce Samochód z udokumentowaną historią serwisową Wyposażenie (wybrane elementy): - Silnik: 2.2 i-CDTi (140 KM, 340 Nm) - Skrzynia Manualna 6 biegowa - Napęd na przednią oś - Rozrząd na łańcuchu - koła aluminiowe 17-calowe oryginalne z salonu - Lakier czarny perłowy - Tapicerka materiałowa - Wykończenie wnętrza plastik + aluminium - Fotele z możliwością regulacji - Kanapa z dostępem do przestrzeni załadunkowej i podłokietnikiem - Kierownica multimedialna obszyta skórą - Klimatyzacja automatyczna jednostrefowa - Światła przeciwmgłowe przednie i tylne - Tempomat - Czujnik zmierzchu - Czujnik deszczu - Elektrycznie regulowane lusterka - Elektryczne szyby przednie i tylne - system Isofix - wentylowany schowek - Radio na płytę - System Honda komputera pokładowego - lusterka boczne składane + podgrzewane - System ściemniania ekranu podczas nocnej jazdy - Tylne czujniki parkowania Samochód osobiście przywiozłem od pierwszego właściciela w Niemczech w roku 2018 i jestem pierwszym właścicielem w Polsce. Auto było przeze mnie użytkowane od 2018 roku do chwili obecnej. Podczas zakupu samochodu przebieg wynosił 167 tysięcy , na chwilę obecną przebieg samochodu to 214 tysięcy. Obecnie auto posiada na sobie opony z roku 2022 z dużą ilością bieżnika, auto jest ubezpieczone do roku 2025 do października. Przegląd Techniczny robiony był w listopadzie 2024. Stan samochodu uważam na bardzo dobry bez wkładu finansowego. * Regularnie wymieniałem olej 5w-30 (1 raz w roku max do 10 tysięcy km) * Co roku wymieniane były filtr (olejowy, kabinowy, paliwa, powietrza) * Olej w skrzyni biegów wymieniałem 2 razy podczas swojego użytkowania * Klocki hamulcowe zmieniane były 2 razy * Tarcze hamulcowe zmieniane były 2 razy * Akumulator wymieniony został w roku 2023 * Samochód posiada wykupione ubezpieczenie OC do 10.2025 * Samochód posiada aktualny przegląd techniczny do 09.2025 W cenie zawarte jest: * 4 sztuki opon letnich Viking, zakupionych przeze mnie w roku 2024 * Koło dojazdowe * Transmiter do puszczania muzyki z telefonu Powodem sprzedaży jest zmiana samochodu na auto dostawcze. Na prośbę kupującego istnieje możliwość sprawdzenia stanu technicznego samochodu w dowolnie wybranym serwisie na terenie Świnoujścia Lokalizacja: Zachodniopomorskie, Świnoujście Szczegóły udzielam telefoniczne pod nr tel.  Marcel Kopaczewski.",
        url = "https://www.otomoto.pl/osobowe/oferta/honda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
        photoPath = "C:\\Users\\katya\\Desktop\\otomoto\\car_photos\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
        htmlPath = "C:\\Users\\katya\\Desktop\\otomoto\\car_htmls\\https%3A%2F%2Fwww.otomoto.pl%2Fosobowe%2Foferta%2Fhonda-civic-honda-civic-viii-2-2i-ctdi-sport-zadbany-egzemplarz-i-bezwypadkowy-ID6H0Xm8.html",
        location = Location(12,1500,50.47625,17.33254)
    )


    AppTheme(dynamicColor = false) {
        DropDownMenu(carSpecs = carSpecs, textMenu = "Basic")

    }
}
