package com.example.ecohand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ecohand.navigation.AppNavigation
import com.example.ecohand.ui.theme.EcohandTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcohandTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}