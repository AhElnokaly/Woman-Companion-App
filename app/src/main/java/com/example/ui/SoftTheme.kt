package com.example.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AppLayers
import com.example.ui.theme.Primitives

// --- Custom Colors for Woman Companion Theme mapped to Design Tokens ---
object SoftTheme {
    private val _isDark = mutableStateOf(true)
    var isDark: Boolean
        get() = _isDark.value
        set(value) { _isDark.value = value }

    val DeepSlate: Color
        get() = if (isDark) Primitives.CosmicNavy else Primitives.BlossomCream

    val CardSlate: Color
        get() = AppLayers.getContainerSurface(isDark)

    val LightPink: Color
        get() = if (isDark) Primitives.LightPinkGlow else Primitives.SoftRoseLight

    val SoftPink: Color
        get() = AppLayers.getInteractiveAccent(isDark)

    val DeepPink: Color
        get() = if (isDark) Primitives.CoralRoseDark else Primitives.MagentaPinkLight

    val MintTeal: Color
        get() = if (isDark) Primitives.OceanMintDark else Primitives.ForestTealLight

    val SoftTeal: Color
        get() = if (isDark) Primitives.SeafoamMintDark else Primitives.EmeraldTealLight

    val GoldFasting: Color
        get() = if (isDark) Primitives.AmberWarningDark else Primitives.AmberWarningLight

    val RedDanger: Color
        get() = if (isDark) Primitives.RedErrorDark else Primitives.RedErrorLight

    val SoftGray: Color
        get() = if (isDark) Primitives.NeutralGrayDark else Primitives.NeutralGrayLight

    val PregnancyPurple: Color
        get() = if (isDark) Color(0xFFBA68C8) else Color(0xFF8E24AA)

    val NifasRose: Color
        get() = if (isDark) Color(0xFFFF80AB) else Color(0xFFE91E63)

    val TextWhite: Color
        get() = if (isDark) Color(0xFFF5F6F8) else Primitives.SlateBlueLight

    val BackgroundBrush: Brush
        get() = AppLayers.getBaseCanvas(isDark)
}
