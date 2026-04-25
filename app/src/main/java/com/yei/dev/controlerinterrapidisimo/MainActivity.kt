package com.yei.dev.controlerinterrapidisimo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yei.dev.controlerinterrapidisimo.presentation.navigation.NavWrapper
import com.yei.dev.controlerinterrapidisimo.ui.theme.ControlerInterrapidisimoTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the Controller APP.
 * Configured with Hilt for dependency injection and Navigation 3 for screen navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControlerInterrapidisimoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavWrapper()
                }
            }
        }
    }
}