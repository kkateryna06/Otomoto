package com.example.otomotoapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

fun getMinMax(data: MinMaxResponse?): Pair<Float, Float> =
    (data?.min_max_values?.getOrNull(0)?.toString()?.toFloatOrNull() ?: 0f) to
            (data?.min_max_values?.getOrNull(1)?.toString()?.toFloatOrNull() ?: 0f)

fun getUnique(data: UniqueValueResponse?) =
    data?.unique_values ?: emptyList()

@Composable
fun FilterScreen(viewModel: MainViewModel, isSpecialEnabled: Boolean, navController: NavHostController) {
    var filterData by remember { mutableStateOf<FilterData?>(null) }
    val markData by viewModel.getUniqueValues("mark").observeAsState()
    val modelData by viewModel.getUniqueValues("model").observeAsState()
    val priceData by viewModel.getMinMaxValues("price").observeAsState()
    val yearData by viewModel.getMinMaxValues("year").observeAsState()
    val bodyTypeData by viewModel.getUniqueValues("body_type").observeAsState()
    val mileageData by viewModel.getMinMaxValues("mileage").observeAsState()
    val fuelTypeData by viewModel.getUniqueValues("fuel_type").observeAsState()
    val engineCapacityData by viewModel.getMinMaxValues("engine_capacity").observeAsState()
    val enginePowerData by viewModel.getMinMaxValues("engine_power").observeAsState()
    val urbanConsumptionData by viewModel.getMinMaxValues("urban_consumption").observeAsState()
    val extraUrbanConsumptionData by viewModel.getMinMaxValues("extra_urban_consumption").observeAsState()

    val allDataLoaded = listOf(
        markData, modelData, priceData, yearData, bodyTypeData, mileageData, fuelTypeData,
        engineCapacityData, enginePowerData, urbanConsumptionData, extraUrbanConsumptionData
    ).all { it != null }

    LaunchedEffect(allDataLoaded) {
        if (allDataLoaded) {
            val newFilterData = FilterData(
                markList = getUnique(markData),
                modelList = getUnique(modelData),
                maxPrice = getMinMax(priceData).second,
                minYear = getMinMax(yearData).first,
                maxYear = getMinMax(yearData).second,
                bodyTypeList = getUnique(bodyTypeData),
                minMileage = getMinMax(mileageData).first,
                maxMileage = getMinMax(mileageData).second,
                fuelTypeList = getUnique(fuelTypeData),
                minEngineCapacity = getMinMax(engineCapacityData).first,
                maxEngineCapacity = getMinMax(engineCapacityData).second,
                minEnginePower = getMinMax(enginePowerData).first,
                maxEnginePower = getMinMax(enginePowerData).second,
                minUrbanConsumption = getMinMax(urbanConsumptionData).first,
                maxUrbanConsumption = getMinMax(urbanConsumptionData).second,
                minExtraUrbanConsumption = getMinMax(extraUrbanConsumptionData).first,
                maxExtraUrbanConsumption = getMinMax(extraUrbanConsumptionData).second
            )

            if (filterData != newFilterData) {
                filterData = newFilterData
            }
        }
    }

    Log.d("FilterData", "filterData = $filterData")

    if (filterData == null) {
        CircularProgressIndicator()
    } else {
        test(filterData = filterData!!, navController = navController, viewModel = viewModel)
    }
}

@Composable
fun test(filterData: FilterData,
         navController: NavHostController,
         viewModel: MainViewModel) {
    val minPriceSlider = remember { mutableStateOf(0f) }
    val maxPriceSlider = remember { mutableStateOf(filterData.maxPrice) }


    maxPriceSlider.value = filterData.maxPrice


    val minYearInput = remember { mutableStateOf("1990") }
    val maxYearInput = remember { mutableStateOf("2025") }

    val minMileageInput = remember { mutableStateOf(filterData.minMileage.toInt().toString()) }
    val maxMileageInput = remember { mutableStateOf(filterData.maxMileage.toInt().toString()) }

    val minEngineCapacityInput = remember { mutableStateOf(filterData.minEngineCapacity.toInt().toString()) }
    val maxEngineCapacityInput = remember { mutableStateOf(filterData.maxEngineCapacity.toInt().toString()) }

    val minUrbanConsumptionInput = remember { mutableStateOf(filterData.minUrbanConsumption.toString()) }
    val maxUrbanConsumptionInput = remember { mutableStateOf(filterData.maxUrbanConsumption.toString()) }

    val minExtraUrbanConsumptionInput = remember { mutableStateOf(filterData.minExtraUrbanConsumption.toString()) }
    val maxExtraUrbanConsumptionInput = remember { mutableStateOf(filterData.maxExtraUrbanConsumption.toString()) }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Filters", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(text = "Price", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                PriceRangeSlider(minPriceSlider, maxPriceSlider, filterData.maxPrice)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Mark", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                CheckboxGroup(filterData.markList)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Model", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                CheckboxGroup(filterData.modelList)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Year", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                RangeFilter(minYearInput, maxYearInput, viewModel)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Body type", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                CheckboxGroup(filterData.bodyTypeList)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Mileage", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                RangeFilter(minMileageInput, maxMileageInput, viewModel)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Fuel type", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                CheckboxGroup(filterData.fuelTypeList)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Engine capacity", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                RangeFilter(minEngineCapacityInput, maxEngineCapacityInput, viewModel)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Urban consumption", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                RangeFilter(minUrbanConsumptionInput, maxUrbanConsumptionInput, viewModel)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Extra urban consumption", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                RangeFilter(minExtraUrbanConsumptionInput, maxExtraUrbanConsumptionInput, viewModel)
                Spacer(modifier = Modifier.height(25.dp))
            }

            BottomFilterBar(navController = navController)
        }
    }
}

@Composable
fun PriceRangeSlider(
    minPriceSlider: MutableState<Float>,
    maxPriceSlider: MutableState<Float>,
    maxPrice: Float
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        RangeSlider(
            value = minPriceSlider.value..maxPriceSlider.value,
            onValueChange = { minPriceSlider.value = it.start; maxPriceSlider.value = it.endInclusive },
            valueRange = 0f..maxPrice
        )

        Spacer (modifier = Modifier.height(5.dp))

        Row (horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column (horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.5f)) {
                TextField(
                    value = minPriceSlider.value.toInt().toString(),
                    onValueChange = { minPriceSlider.value = it.toFloat() },
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                        .height(50.dp)
                )
                Text("PLN")
            }
            Spacer(modifier = Modifier.weight(1f))
            Column (horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.5f)) {
                TextField(
                    value = maxPriceSlider.value.toInt().toString(),
                    onValueChange = { maxPriceSlider.value = it.toFloat() },
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                        .height(50.dp)

                )
                Text("PLN")
            }
        }
    }
}

@Composable
fun CheckboxGroup(elementList: List<String>) {
    if (elementList.size > 5) {
        val showMoreExpanded = remember { mutableStateOf(false) }
        Column {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!showMoreExpanded.value) {
                    Text(text = "Show more")
                    IconButton(onClick = { showMoreExpanded.value = !showMoreExpanded.value }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_down),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                } else {
                    Text(text = "Show less")
                    IconButton(onClick = { showMoreExpanded.value = !showMoreExpanded.value }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_up),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
            if (!showMoreExpanded.value) {
                CheckboxGroupElements(elementList.take(5))
            } else {
                CheckboxGroupElements(elementList)
            }
        }
    } else {
        CheckboxGroupElements(elementList)
    }
}

@Composable
fun CheckboxGroupElements(elementList: List<String>) {
    val checkedState = remember { mutableStateMapOf<String, Boolean>().apply {
        elementList.forEach { this[it] = false }
    } }

    Column {
        elementList.forEach { item ->
            val isChecked = checkedState[item] ?: false

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .size(25.dp)
                    .border(width = 1.dp, color = Color.Black)
                    .background(color = if (isChecked) MaterialTheme.colorScheme.onSecondaryContainer else Color.Transparent)
                    .clickable(onClick = {
                        checkedState[item] = !isChecked
                    })
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = item)
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
fun RangeFilter(minValue: MutableState<String>, maxValue: MutableState<String>, viewModel: MainViewModel) {
    Row(horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "From")
            TextField(
                value = minValue.value,
                onValueChange = {
                    minValue.value = it
                    viewModel.updateFilterOptions {
                        copy()
                    }
                                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp),
                modifier = Modifier.height(50.dp)
            )
        }
        Spacer(modifier = Modifier.width(30.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "To")
            TextField(
                value = maxValue.value,
                onValueChange = { maxValue.value = it },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp),
                modifier = Modifier.height(50.dp)
            )
        }
    }
}

@Composable
fun BottomFilterBar(navController: NavHostController, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    ) {
        Button(onClick = { navController.navigate(Screen.MainScreen.route) }) {
            Text(text = "Apply Filters")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun FilterMenuPreview() {
    val isFilterMenuExpanded = remember { mutableStateOf(true) }
    val marks = listOf("Rio", "Lancer", "Focus", "Megane", "Mondeo", "M4", "306")
    val models = listOf("Rio", "Lancer", "Focus", "Megane", "Mondeo", "M4", "306")
    val maxPrice = 30000f
    val bodyTypes = listOf("Coupe", "kombi", "SUV", "Sedan")
    val fuelTypes = listOf("Benzyna", "Diesel", "Elektryczny")

    AppTheme(dynamicColor = false) {
        Box() {
//            test(marks, models, maxPrice, bodyTypes, fuelTypes)
        }
    }
}