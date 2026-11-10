package com.maziwasmart.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.maziwasmart.app.ui.navigation.MaziwaApp
import com.maziwasmart.app.ui.theme.MaziwaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // white status bar icons on the green header
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            MaziwaTheme {
                MaziwaApp()
            }
        }
    }
}
