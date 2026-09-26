package com.example.gymsharktest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gymsharktest.ui.navigation.GymsharkNavHost
import com.example.gymsharktest.ui.theme.GymsharkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            GymsharkTheme {
                GymsharkNavHost()
            }
        }
    }
}
