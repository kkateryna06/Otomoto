package com.example.otomotoapp

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.compose.AppTheme

@Composable
fun TopAppBar(isSpecialCarEnabled: Boolean, viewModel: MainViewModel, navController: NavHostController){
    val isFilterMenuExpanded = remember { mutableStateOf(false) }

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
    Column {
        SearchField(isFilterMenuExpanded, navController = navController)
    }

}

@Composable
fun SearchField(isFilterMenuExpanded: MutableState<Boolean>, navController: NavHostController) {
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

            IconButton(onClick = {
                navController.navigate(Screen.FilterScreen.withArgs(isFilterMenuExpanded.value))
            }, modifier = Modifier.size(24.dp)) {
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
                textStyle = TextStyle(fontSize = 15.sp),
                modifier = Modifier.height(50.dp),
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


@Preview(showBackground = true)
@Composable
fun TopAppBarPreview() {
    AppTheme(dynamicColor = false) {
        Column(modifier = Modifier.fillMaxSize()) {
//            TopAppBar(isSpecialCarEnabled = false, viewModel = MainViewModel())
        }
    }
}