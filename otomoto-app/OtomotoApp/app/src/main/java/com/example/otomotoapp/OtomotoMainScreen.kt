package com.example.otomotoapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtomotoMainScreen(viewModel: MainViewModel) {
        val carList by viewModel.carList.observeAsState(emptyList())
        val errorMessage by viewModel.errorMessage.observeAsState("")

        LaunchedEffect(Unit) {
            viewModel.getCarSpecs()
        }

        if (errorMessage?.isNotEmpty() == true) {
            Text(text = errorMessage!!, color = Color.Red)
        }
    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar()
        CarAd(carList)
    }
}

@Composable
fun TopAppBar(){
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically) {
        Text(text ="Otomoto Cars",
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun CarAd(adds: List<CarSpecs>) {
    Row{
        LazyVerticalGrid(GridCells.Fixed(2)) {
            items(adds) {
                item ->
                AdItem(item)
            }
        }
    }
}

@Composable
fun AdItem(carSpecs: CarSpecs) {
    Column(modifier = Modifier.padding(8.dp)) {
        Image(painter = painterResource(id = R.drawable.no_image), contentDescription = "car photo")
        Text(text = "${carSpecs.mark} ${carSpecs.model} ${carSpecs.version?:""}", style = TextStyle(fontWeight = FontWeight.Bold))
        Text(text = "${carSpecs.price} PLN")
        Text(text =  "${carSpecs.mileage} km •  ${carSpecs.year} • ${carSpecs.body_type}")
        Text(text = carSpecs.date.replace('T', ' ').replace('Z', ' '))
    }
}
