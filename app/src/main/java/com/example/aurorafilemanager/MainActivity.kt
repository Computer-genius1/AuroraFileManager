package com.example.aurorafilemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aurorafilemanager.model.ThemeMode
import com.example.aurorafilemanager.ui.MainScreen
import com.example.aurorafilemanager.ui.OnboardingScreen
import com.example.aurorafilemanager.ui.UpdateCompleteScreen
import com.example.aurorafilemanager.ui.theme.AuroraTheme
import com.example.aurorafilemanager.viewmodel.FileExplorerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FileExplorerViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            AuroraTheme(darkTheme = isDark) {
                val hasSeenOnboarding by viewModel.hasSeenOnboarding.collectAsStateWithLifecycle()
                val shouldShowUpdateComplete by viewModel.shouldShowUpdateComplete.collectAsStateWithLifecycle()

                if (!hasSeenOnboarding) {
                    OnboardingScreen(
                        onContinue = { viewModel.completeOnboarding() }
                    )
                } else if (shouldShowUpdateComplete) {
                    UpdateCompleteScreen(
                        onDismiss = { viewModel.dismissUpdateComplete() }
                    )
                } else {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}