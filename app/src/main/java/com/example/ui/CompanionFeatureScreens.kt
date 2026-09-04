package com.example.ui

import androidx.compose.runtime.Composable
import com.example.ui.companion.*
import com.example.viewmodel.WomanCompanionViewModel

// ==============================================================================
// Modular Facade for Companion Feature Screens
// Sub-screens have been modularized into:
// - com.example.ui.companion.PartnerSyncScreen
// - com.example.ui.companion.HomePharmacyScreen
// - com.example.ui.companion.CravingScreen
// - com.example.ui.companion.JouriNotificationsAndUpdates (NewFeaturesUpdatesBanner, JouriNotificationsDialog, SmartAlertCard)
// ==============================================================================

@Composable
fun LegacyPartnerSyncScreen(viewModel: WomanCompanionViewModel) {
    PartnerSyncScreen(viewModel = viewModel)
}

@Composable
fun LegacyHomePharmacyScreen(viewModel: WomanCompanionViewModel) {
    HomePharmacyScreen(viewModel = viewModel)
}

@Composable
fun LegacyCravingScreen(viewModel: WomanCompanionViewModel) {
    CravingScreen(viewModel = viewModel)
}
