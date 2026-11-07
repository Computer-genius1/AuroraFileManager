package com.example.aurorafilemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
            AuroraTheme {
                val viewModel: FileExplorerViewModel = viewModel()
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