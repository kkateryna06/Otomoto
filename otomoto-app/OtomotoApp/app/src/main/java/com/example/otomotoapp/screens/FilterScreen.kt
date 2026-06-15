package com.example.otomotoapp.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.otomotoapp.data.FilterData
import com.example.otomotoapp.MainViewModel
import com.example.otomotoapp.R

@Composable
fun FilterScreen(viewModel: MainViewModel, navController: NavHostController) {
    val draftFilterData by viewModel.draftFilterData.collectAsState()
    val baseFilterData by viewModel.baseFilterData.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFilterMetadata()
    }

    if (baseFilterData == null) {
        CircularProgressIndicator()
    } else {
        test(filterData = draftFilterData ?: baseFilterData!!, baseFilterData = baseFilterData!!, navController = navController, viewModel = viewModel)
    }
}


@Composable
fun test(filterData: FilterData,
         baseFilterData: FilterData,
         navController: NavHostController,
         viewModel: MainViewModel
) {
    val priceSliderMax = maxOf(baseFilterData.maxPrice, filterData.maxPrice, DEFAULT_MAX_PRICE)
    val minPriceSlider = remember(filterData.minPrice) { mutableStateOf(filterData.minPrice) }
    val maxPriceSlider = remember(filterData.maxPrice, priceSliderMax) {
        mutableStateOf(filterData.maxPrice.takeIf { it > 0f } ?: priceSliderMax)
    }


    val minYearInput = remember(filterData.minYear) { mutableStateOf(filterData.minYear.toInt().toString()) }
    val maxYearInput = remember(filterData.maxYear) { mutableStateOf(filterData.maxYear.toInt().toString()) }

    val minMileageInput = remember(filterData.minMileage) { mutableStateOf(filterData.minMileage.toInt().toString()) }
    val maxMileageInput = remember(filterData.maxMileage) { mutableStateOf(filterData.maxMileage.toInt().toString()) }

    val minEngineCapacityInput = remember(filterData.minEngineCapacity) { mutableStateOf(filterData.minEngineCapacity.toInt().toString()) }
    val maxEngineCapacityInput = remember(filterData.maxEngineCapacity) { mutableStateOf(filterData.maxEngineCapacity.toInt().toString()) }

    val minEnginePowerInput = remember(filterData.minEnginePower) { mutableStateOf(filterData.minEnginePower.toInt().toString()) }
    val maxEnginePowerInput = remember(filterData.maxEnginePower) { mutableStateOf(filterData.maxEnginePower.toInt().toString()) }
    val queryInput = remember(filterData.q) { mutableStateOf(filterData.q) }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.resetUserFilters() },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Clear filters")
                }

                Text(text = "Price", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                PriceRangeSlider(minPriceSlider, maxPriceSlider, priceSliderMax, viewModel)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Brand", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                CheckboxGroup(onAddFilterItem = { item ->
                    viewModel.addBrandFilter(item)
                }, onRemoveFilterItem = { item ->
                    viewModel.removeBrandFilter(item)
                }, elementList = baseFilterData.brandList, userElementList =  filterData.brandList, viewModel =  viewModel)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Model", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                if (filterData.brandList.isEmpty()) {
                    Text(
                        text = "Select a brand to choose a model",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    CheckboxGroup(onAddFilterItem = { item ->
                        viewModel.addToFilterList(
                            selector = { modelList },
                            item = item,
                            updater = { copy(modelList = it) }
                        )
                    }, onRemoveFilterItem = { item ->
                        viewModel.removeFromFilterList(
                            selector = { modelList },
                            item = item,
                            updater = { copy(modelList = it) }
                        )
                    }, elementList = baseFilterData.modelList, userElementList = filterData.modelList, viewModel = viewModel)
                }
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Year", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                RangeFilter(
                    minValue = minYearInput,
                    maxValue = maxYearInput,
                    viewModel = viewModel,
                    updateMinValue = { value ->
                        viewModel.updateFilterData {
                            copy(minYear = value)
                        }
                    },
                    updateMaxValue = { value ->
                        viewModel.updateFilterData {
                            copy(maxYear = value)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Body type", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                CheckboxGroup(onAddFilterItem = { item ->
                    viewModel.addToFilterList(
                        selector = {bodyTypeList},
                        item = item,
                        updater = { copy(bodyTypeList = it) }
                    )
                }, onRemoveFilterItem = { item ->
                    viewModel.removeFromFilterList(
                        selector = {bodyTypeList},
                        item = item,
                        updater = { copy(bodyTypeList = it) }
                    )
                }, elementList =  baseFilterData.bodyTypeList, userElementList =  filterData.bodyTypeList, viewModel =  viewModel)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Mileage", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                RangeFilter(
                    minValue = minMileageInput,
                    maxValue = maxMileageInput,
                    viewModel = viewModel,
                    updateMinValue = { value ->
                        viewModel.updateFilterData {
                            copy(minMileage = value)
                        }
                    },
                    updateMaxValue = { value ->
                        viewModel.updateFilterData {
                            copy(maxMileage = value)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Fuel type", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                CheckboxGroup(onAddFilterItem = { item ->
                    viewModel.addToFilterList(
                        selector = {fuelTypeList},
                        item = item,
                        updater = {copy(fuelTypeList = it)}
                    )
                }, onRemoveFilterItem = { item ->
                    viewModel.removeFromFilterList(
                        selector = {fuelTypeList},
                        item = item,
                        updater = {copy(fuelTypeList = it)}
                    )
                }, elementList = baseFilterData.fuelTypeList ,userElementList = filterData.fuelTypeList, viewModel =  viewModel)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Gearbox", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                CheckboxGroup(onAddFilterItem = { item ->
                    viewModel.addToFilterList(
                        selector = {gearboxList},
                        item = item,
                        updater = {copy(gearboxList = it)}
                    )
                }, onRemoveFilterItem = { item ->
                    viewModel.removeFromFilterList(
                        selector = {gearboxList},
                        item = item,
                        updater = {copy(gearboxList = it)}
                    )
                }, elementList = baseFilterData.gearboxList, userElementList = filterData.gearboxList, viewModel = viewModel)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Transmission", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                CheckboxGroup(onAddFilterItem = { item ->
                    viewModel.addToFilterList(
                        selector = {transmissionList},
                        item = item,
                        updater = {copy(transmissionList = it)}
                    )
                }, onRemoveFilterItem = { item ->
                    viewModel.removeFromFilterList(
                        selector = {transmissionList},
                        item = item,
                        updater = {copy(transmissionList = it)}
                    )
                }, elementList = baseFilterData.transmissionList, userElementList = filterData.transmissionList, viewModel = viewModel)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Seller type", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                CheckboxGroup(onAddFilterItem = { item ->
                    viewModel.addToFilterList(
                        selector = {sellerTypeList},
                        item = item,
                        updater = {copy(sellerTypeList = it)}
                    )
                }, onRemoveFilterItem = { item ->
                    viewModel.removeFromFilterList(
                        selector = {sellerTypeList},
                        item = item,
                        updater = {copy(sellerTypeList = it)}
                    )
                }, elementList = baseFilterData.sellerTypeList, userElementList = filterData.sellerTypeList, viewModel = viewModel)
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Engine capacity", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                RangeFilter(minEngineCapacityInput, maxEngineCapacityInput, viewModel,
                    updateMinValue = { value ->
                        viewModel.updateFilterData {
                            copy(minEngineCapacity = value)
                        }
                    },
                    updateMaxValue = { value ->
                        viewModel.updateFilterData {
                            copy(maxEngineCapacity = value)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(25.dp))

                Text(text = "Engine power", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                RangeFilter(minEnginePowerInput, maxEnginePowerInput, viewModel,
                    updateMinValue = { value ->
                        viewModel.updateFilterData {
                            copy(minEnginePower = value)
                        }
                    },
                    updateMaxValue = { value ->
                        viewModel.updateFilterData {
                            copy(maxEnginePower = value)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(25.dp))
            }
        }
    }
}

@Composable
fun PriceRangeSlider(
    minPriceSlider: MutableState<Float>,
    maxPriceSlider: MutableState<Float>,
    maxBasePrice: Float,
    viewModel: MainViewModel
) {
    Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
        RangeSlider(
            value = minPriceSlider.value..maxPriceSlider.value,
            onValueChange = {
                minPriceSlider.value = it.start.roundToPriceStep()
                maxPriceSlider.value = it.endInclusive.roundToPriceStep()
                viewModel.updateFilterData { copy(minPrice = minPriceSlider.value) }
                viewModel.updateFilterData { copy(maxPrice = maxPriceSlider.value) }
                },

            valueRange = 0f..maxBasePrice
        )

        Spacer (modifier = Modifier.height(5.dp))

        Row (horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column (horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)) {
                TextField(
                    value = minPriceSlider.value.toInt().toString(),
                    onValueChange = {
                        val newValue = it.toFloatOrNull()
                        if (newValue != null) {
                            minPriceSlider.value = newValue
                            viewModel.updateFilterData {
                                copy(minPrice = newValue)
                            }
                        }
                                    },
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                        .height(50.dp)
                )
                Text("PLN", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column (horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)) {
                TextField(
                    value = maxPriceSlider.value.toInt().toString(),
                    onValueChange = {
                        val newValue = it.toFloatOrNull()
                        if (newValue != null) {
                            maxPriceSlider.value = newValue
                            viewModel.updateFilterData {
                                copy(maxPrice = newValue)
                            }
                        }
                                    },
                    textStyle = TextStyle(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                        .height(50.dp)

                )
                Text("PLN", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private const val DEFAULT_MAX_PRICE = 100_000f
private const val PRICE_STEP = 1_000f

private fun Float.roundToPriceStep(): Float =
    (this / PRICE_STEP).toInt() * PRICE_STEP

@Composable
fun CheckboxGroup(elementList: List<String>, userElementList: List<String>, onAddFilterItem: (String)->Unit, onRemoveFilterItem: (String)->Unit, viewModel: MainViewModel) {
    if (elementList.size > 5) {
        val showMoreExpanded = remember { mutableStateOf(false) }
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!showMoreExpanded.value) {
                    TextButton(onClick = { showMoreExpanded.value = !showMoreExpanded.value }) {
                        Text(text = "Show more")
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_down),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    TextButton(onClick = { showMoreExpanded.value = !showMoreExpanded.value }) {
                        Text(text = "Show less")
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_up),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            if (!showMoreExpanded.value) {
                CheckboxGroupElements(elementList.take(5), userElementList, onAddFilterItem, onRemoveFilterItem, viewModel)
            } else {
                CheckboxGroupElements(elementList, userElementList, onAddFilterItem, onRemoveFilterItem, viewModel)
            }
        }
    } else {
        CheckboxGroupElements(elementList, userElementList, onAddFilterItem, onRemoveFilterItem, viewModel)
    }
}

@Composable
fun CheckboxGroupElements(elementList: List<String>, userElementList: List<String>, onAddFilterItem: (String)->Unit, onRemoveFilterItem: (String)->Unit, viewModel: MainViewModel) {
    val checkedState = remember(elementList, userElementList) { mutableStateMapOf<String, Boolean>().apply {
        elementList.forEach { this[it] = userElementList.contains(it)}
    } }

    Column {
        elementList.forEach { item ->
            val isChecked = checkedState[item] ?: false
            val rowShape = RoundedCornerShape(8.dp)
            val rowBackground = if (isChecked) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
            val rowBorder = if (isChecked) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 36.dp)
                    .clip(rowShape)
                    .background(rowBackground)
                    .clickable {
                        checkedState[item] = !isChecked
                        if (!isChecked) {
                            onAddFilterItem(item)
                        } else {
                            onRemoveFilterItem(item)
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 32.dp) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            checkedState[item] = checked
                            if (checked) {
                                onAddFilterItem(item)
                            } else {
                                onRemoveFilterItem(item)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isChecked) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
fun RangeFilter(minValue: MutableState<String>, maxValue: MutableState<String>, viewModel: MainViewModel,
                updateMinValue: (Float) -> Unit, updateMaxValue: (Float) -> Unit) {
    Row(horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "From", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextField(
                value = minValue.value,
                onValueChange = {
                    minValue.value = it
                    updateMinValue(it.toFloatOrNull() ?: 0f)
                                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(50.dp)
            )
        }
        Spacer(modifier = Modifier.width(30.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "To", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextField(
                value = maxValue.value,
                onValueChange = {
                    maxValue.value = it
                    updateMaxValue(it.toFloatOrNull() ?: 0f)
                                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(50.dp)
            )
        }
    }
}
