package com.example.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AppLayers
import com.example.ui.theme.ModernHealthTokens
import com.example.ui.theme.Primitives

// --- Custom Colors for Woman Companion Theme mapped to Design Tokens ---
object SoftTheme {
    private val _isDark = mutableStateOf(false)
    var isDark: Boolean
        get() = _isDark.value
        set(value) { _isDark.value = value }

    val DeepSlate: Color
        get() = if (isDark) Primitives.CosmicNavy else ModernHealthTokens.CanvasBackground

    val CardSlate: Color
        get() = if (isDark) Primitives.MidnightSlate else ModernHealthTokens.CardWhite

    val CardDark: Color
        get() = if (isDark) Primitives.MidnightSlate else ModernHealthTokens.CardWhite

    val ElevatedSlate: Color
        get() = if (isDark) Primitives.ElevatedSlate else ModernHealthTokens.AccentMint

    val LightPink: Color
        get() = if (isDark) Primitives.LightPinkGlow else ModernHealthTokens.WomenPinkBg

    val SoftPink: Color
        get() = if (isDark) Primitives.VibrantRoseDark else ModernHealthTokens.WomenPink

    val PrimaryPink: Color
        get() = if (isDark) Primitives.VibrantRoseDark else ModernHealthTokens.WomenPink

    val CardBorder: Color
        get() = if (isDark) Color(0x33FA809F) else ModernHealthTokens.CardBorderLight

    val DeepPink: Color
        get() = if (isDark) Primitives.CoralRoseDark else ModernHealthTokens.WomenPinkDark

    val MintTeal: Color
        get() = if (isDark) Primitives.OceanMintDark else ModernHealthTokens.PrimaryEmerald

    val SoftTeal: Color
        get() = if (isDark) Primitives.SeafoamMintDark else ModernHealthTokens.PrimaryTeal

    val GoldFasting: Color
        get() = if (isDark) Primitives.AmberWarningDark else Primitives.AmberWarningLight

    val RedDanger: Color
        get() = if (isDark) Primitives.RedErrorDark else Primitives.RedErrorLight

    val SoftGray: Color
        get() = if (isDark) Primitives.NeutralGrayDark else ModernHealthTokens.TextSecondary

    val PregnancyPurple: Color
        get() = if (isDark) Color(0xFFBA68C8) else ModernHealthTokens.SleepPurple

    val SoftPurple: Color
        get() = if (isDark) Color(0xFFBA68C8) else ModernHealthTokens.SleepPurple

    val WarmCoral: Color
        get() = if (isDark) Color(0xFFFF8A65) else ModernHealthTokens.FoodOrange

    val NifasRose: Color
        get() = if (isDark) Color(0xFFFF80AB) else ModernHealthTokens.WomenPink

    val TextWhite: Color
        get() = if (isDark) Color(0xFFF5F6F8) else ModernHealthTokens.TextPrimaryDark

    val BackgroundBrush: Brush
        get() = AppLayers.getBaseCanvas(isDark)

    // --- Modern Health & Wellness Tokens ---
    val CanvasBg: Color get() = if (isDark) Primitives.CosmicNavy else ModernHealthTokens.CanvasBackground
    val CardBg: Color get() = if (isDark) Primitives.MidnightSlate else ModernHealthTokens.CardWhite
    val EmeraldPrimary: Color get() = ModernHealthTokens.PrimaryEmerald
    val TealDark: Color get() = ModernHealthTokens.PrimaryTeal
    val MintAccent: Color get() = if (isDark) Color(0xFF1B3B36) else ModernHealthTokens.AccentMint
    val MintAccentBorder: Color get() = if (isDark) Color(0xFF2E635B) else ModernHealthTokens.AccentMintBorder

    // Category Colors
    val WaterBlue: Color get() = ModernHealthTokens.WaterBlue
    val WaterBg: Color get() = if (isDark) Color(0xFF152A38) else ModernHealthTokens.WaterBg
    val FoodOrange: Color get() = ModernHealthTokens.FoodOrange
    val FoodBg: Color get() = if (isDark) Color(0xFF382914) else ModernHealthTokens.FoodBg
    val SleepPurple: Color get() = ModernHealthTokens.SleepPurple
    val SleepBg: Color get() = if (isDark) Color(0xFF222038) else ModernHealthTokens.SleepBg
    val SymptomsPurple: Color get() = ModernHealthTokens.SymptomsPurple
    val SymptomsBg: Color get() = if (isDark) Color(0xFF321935) else ModernHealthTokens.SymptomsBg
    val WomenPinkColor: Color get() = ModernHealthTokens.WomenPink
    val WomenPinkBgColor: Color get() = if (isDark) Color(0xFF381827) else ModernHealthTokens.WomenPinkBg
    val JouriTurquoise: Color get() = ModernHealthTokens.JouriTurquoise
    val JouriBg: Color get() = if (isDark) Color(0xFF163330) else ModernHealthTokens.JouriBg
    val TextPrimary: Color get() = if (isDark) Color(0xFFF5F6F8) else ModernHealthTokens.TextPrimaryDark
    val TextSecondaryMuted: Color get() = if (isDark) Primitives.NeutralGrayDark else ModernHealthTokens.TextSecondary
}

