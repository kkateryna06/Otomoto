package com.example.otomotoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.compose.AppTheme
import com.example.otomotoapp.database.Graph

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.provide(context = this)
        setContent {
            AppTheme(dynamicColor = false) {
                Navigation()
            }
        }
    }
}
