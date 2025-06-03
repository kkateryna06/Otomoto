package com.example.otomotoapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.otomotoapp.screen_elements.BottomBar2
import com.example.otomotoapp.screen_elements.SideBar
import com.example.otomotoapp.screen_elements.TopBar2
import kotlinx.coroutines.launch

@Composable
fun OtomotoApp() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val appBarsViewModel: AppBarsViewModel = viewModel()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { SideBar(navController) }
    ) {
        Scaffold(
            topBar = {
                TopBar2(
                    navController = navController,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            },
            bottomBar = { BottomBar2(appBarsViewModel) }
        ) { padding ->
            Navigation(Modifier.padding(padding), navController, appBarsViewModel)
        }
    }
}
