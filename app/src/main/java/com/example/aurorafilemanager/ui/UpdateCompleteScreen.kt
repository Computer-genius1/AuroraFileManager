package com.example.aurorafilemanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aurorafilemanager.R

@Composable
fun UpdateCompleteScreen(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Big Checkmark at the Top
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.update_complete_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.update_complete_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // What's New Feature Cards
                FeatureHighlightCard(
                    icon = Icons.Default.Build,
                    title = "Samsung Compatibility & Bug Fixes",
                    description = stringResource(R.string.changelog_samsung_fix)
                )

                Spacer(modifier = Modifier.height(10.dp))

                FeatureHighlightCard(
                    icon = Icons.Default.DarkMode,
                    title = "System Dark Theme",
                    description = "Automatically adapts to your system night settings so you are never blinded in the dark."
                )

                Spacer(modifier = Modifier.height(10.dp))

                FeatureHighlightCard(
                    icon = Icons.Default.FolderZip,
                    title = "Quick Access Folder Chips",
                    description = "One-tap navigation bar for instant access to Documents, Downloads, Pictures, Videos, and Music."
                )

                Spacer(modifier = Modifier.height(10.dp))

                FeatureHighlightCard(
                    icon = Icons.Default.Edit,
                    title = "Untitled Defaults & Blank Validation",
                    description = "Pre-fills new files/folders with default titles and shows real-time red error validation if blank."
                )

                Spacer(modifier = Modifier.height(10.dp))

                FeatureHighlightCard(
                    icon = Icons.Default.Translate,
                    title = "Full Spanish Support",
                    description = "Complete Spanish localization for all app screens, menus, and onboarding dialogs."
                )

                Spacer(modifier = Modifier.height(10.dp))

                FeatureHighlightCard(
                    icon = Icons.Default.SystemUpdate,
                    title = "Automated Release Checker",
                    description = "Automatically checks GitHub for new releases and prompts you whenever an update drops."
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_explore),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun FeatureHighlightCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}