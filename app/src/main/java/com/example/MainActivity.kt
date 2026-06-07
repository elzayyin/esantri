package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainAppScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SantriViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      // Initialize viewmodel safely using custom Factory
      val viewModel: SantriViewModel = viewModel(
          factory = SantriViewModel.Factory(application)
      )
      val settingsState by viewModel.settings.collectAsState()

      // Dynamically sync theme colors from database-backed Settings
      var primaryThemeColor by remember { mutableStateOf<String?>(null) }
      var secondaryThemeColor by remember { mutableStateOf<String?>(null) }

      // Splash Screen state controller
      var showSplash by remember { mutableStateOf(true) }

      MyApplicationTheme(
          primaryOverride = primaryThemeColor,
          secondaryOverride = secondaryThemeColor
      ) {
          if (showSplash) {
              SplashScreen(
                  settings = settingsState,
                  onTimeout = { showSplash = false }
              )
          } else {
              MainAppScreen(
                  viewModel = viewModel,
                  onColorSettings = { primary, secondary ->
                      primaryThemeColor = primary
                      secondaryThemeColor = secondary
                  }
              )
          }
      }
    }
  }
}
