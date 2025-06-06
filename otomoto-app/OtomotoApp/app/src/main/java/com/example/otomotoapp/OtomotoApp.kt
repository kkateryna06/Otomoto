package com.example.otomotoapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.otomotoapp.screen_elements.BottomBar
import com.example.otomotoapp.screen_elements.SideBar
import com.example.otomotoapp.screen_elements.TopBar
import kotlinx.coroutines.launch

@Composable
fun OtomotoApp() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val appBarsViewModel: AppBarsViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()

    val currentScreen by mainViewModel.currentScreen.observeAsState()

    if (currentScreen != null) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { SideBar(navController, drawerState, scope) }
        ) {
            Scaffold(
                topBar = {
                    TopBar(
                        navController = navController,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        mainViewModel = mainViewModel,
                        title = currentScreen!!.title
                    )
                },
                bottomBar = { BottomBar(appBarsViewModel, currentScreen!!, navController) }
            ) { padding ->
                Column(modifier = Modifier.padding(padding)) {
                    Navigation(navController, appBarsViewModel)
                }
            }
        }
    }
}
