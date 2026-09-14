package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * ============================================================================
 * DESIGN SYSTEM LAYER 1: PRIMITIVE COLOR TOKENS (الألوان الخام الأساسية)
 * ============================================================================
 * These are the absolute raw colors of our palette, carefully chosen based on
 * maternal care best practices (warm roses, cosmic slate navy, calming mint teals).
 */
object Primitives {
    // --- Dark Mode / Cosmic Night Palette ---
    val CosmicNavy = Color(0xFF11151E)     // Deepest canvas background
    val MidnightSlate = Color(0xFF1A1F2B)  // Standard card background
    val ElevatedSlate = Color(0xFF242C3C)  // Sub-cards and active inputs
    val LightPinkGlow = Color(0xFFFFB3C6)  // Delicate highlights
    val VibrantRoseDark = Color(0xFFFA809F) // Glowing primary accent
    val CoralRoseDark = Color(0xFFF0527F)  // Deep intense accent
    val OceanMintDark = Color(0xFF00C0A5)  // Tranquil secondary accent
    val SeafoamMintDark = Color(0xFF80E0D2) // Soft secondary highlight
    
    // --- Light Mode / Rose Blossom Palette ---
    val BlossomCream = Color(0xFFF6FAF8)   // Fresh soft ivory mint tinted off-white
    val PureWhite = Color(0xFFFFFFFF)      // Pure card surfaces
    val SoftAlabaster = Color(0xFFE8F7F2)  // Sub-cards, chips, and inputs
    val SlateBlueLight = Color(0xFF1A2E26) // Rich, high-contrast dark charcoal text
    val SoftRoseLight = Color(0xFFC8F0E4)  // Delicate mint highlights
    val PassionRoseLight = Color(0xFF00A884) // Fresh Emerald/Teal primary accent
    val MagentaPinkLight = Color(0xFF00897B) // Deep rich teal accent
    val ForestTealLight = Color(0xFF00897B) // Natural healing teal
    val EmeraldTealLight = Color(0xFF00A884) // Bright active state green
    
    // --- Shared / Status Palette ---
    val AmberWarningDark = Color(0xFFFFC107)
    val AmberWarningLight = Color(0xFFF57C00)
    val RedErrorDark = Color(0xFFEF5350)
    val RedErrorLight = Color(0xFFD32F2F)
    val NeutralGrayDark = Color(0xFF8E9AA7)
    val NeutralGrayLight = Color(0xFF546E7A)
}

/**
 * ============================================================================
 * DESIGN SYSTEM LAYER 2: SEMANTIC TOKENS (الرموز الدلالية للوظائف)
 * ============================================================================
 * Semantic tokens map physical colors to functional roles. This abstracts the
 * theme so changing a color doesn't require modifying layout code.
 */
interface SemanticPalette {
    val primary: Color
    val secondary: Color
    val tertiary: Color
    val background: Color
    val surface: Color
    val innerSurface: Color
    val textPrimary: Color
    val textSecondary: Color
    val warning: Color
    val error: Color
    val accentBrush: Brush
}

class DarkSemanticPalette : SemanticPalette {
    override val primary = Primitives.VibrantRoseDark
    override val secondary = Primitives.OceanMintDark
    override val tertiary = Primitives.LightPinkGlow
    override val background = Primitives.CosmicNavy
    override val surface = Primitives.MidnightSlate
    override val innerSurface = Primitives.ElevatedSlate
    override val textPrimary = Primitives.LightPinkGlow
    override val textSecondary = Primitives.NeutralGrayDark
    override val warning = Primitives.AmberWarningDark
    override val error = Primitives.RedErrorDark
    override val accentBrush = Brush.verticalGradient(
        colors = listOf(Primitives.VibrantRoseDark, Primitives.CoralRoseDark)
    )
}

class LightSemanticPalette : SemanticPalette {
    override val primary = Primitives.PassionRoseLight
    override val secondary = Primitives.ForestTealLight
    override val tertiary = Primitives.SoftRoseLight
    override val background = Primitives.BlossomCream
    override val surface = Primitives.PureWhite
    override val innerSurface = Primitives.SoftAlabaster
    override val textPrimary = Primitives.SlateBlueLight
    override val textSecondary = Primitives.NeutralGrayLight
    override val warning = Primitives.AmberWarningLight
    override val error = Primitives.RedErrorLight
    override val accentBrush = Brush.verticalGradient(
        colors = listOf(Primitives.PassionRoseLight, Primitives.MagentaPinkLight)
    )
}

/**
 * ============================================================================
 * DESIGN SYSTEM LAYER 3: INTERACTIVE & INTERFACE LAYERS (طبقات وهيكل الواجهات)
 * ============================================================================
 * We divide the screen into 4 distinct physical design layers to create spatial
 * depth, exactly like modern Apple and material glassmorphic designs.
 */
object AppLayers {
    // --- Layer 0: The Base Canvas ---
    // The ultimate foundation. Solid color or background gradients.
    fun getBaseCanvas(isDark: Boolean): Brush {
        return Brush.verticalGradient(
            colors = if (isDark) {
                listOf(Primitives.CosmicNavy, Color(0xFF090B10))
            } else {
                listOf(Primitives.BlossomCream, Color(0xFFEBF0F6))
            }
        )
    }

    // --- Layer 1: Container Surfaces ---
    // Floating cards, primary modules, lists.
    fun getContainerSurface(isDark: Boolean): Color {
        return if (isDark) Primitives.MidnightSlate else Primitives.PureWhite
    }

    // --- Layer 2: Embedded Sub-Surfaces ---
    // Fields, inner chips, progress tracks inside Layer 1.
    fun getEmbeddedSurface(isDark: Boolean): Color {
        return if (isDark) Primitives.ElevatedSlate else Primitives.SoftAlabaster
    }

    // --- Layer 3: Accent Highlights & Interactive Elements ---
    // Glowing borders, primary buttons, ripple colors, selected indicators.
    fun getInteractiveAccent(isDark: Boolean): Color {
        return if (isDark) Primitives.VibrantRoseDark else Primitives.PassionRoseLight
    }
}

object SoftTheme {
    val DeepSlate: Color get() = com.example.ui.SoftTheme.DeepSlate
    val CardSlate: Color get() = com.example.ui.SoftTheme.CardSlate
    val CardDark: Color get() = com.example.ui.SoftTheme.CardDark
    val ElevatedSlate: Color get() = com.example.ui.SoftTheme.ElevatedSlate
    val SoftPink: Color get() = com.example.ui.SoftTheme.SoftPink
    val PrimaryPink: Color get() = com.example.ui.SoftTheme.PrimaryPink
    val DeepPink: Color get() = com.example.ui.SoftTheme.DeepPink
    val LightPink: Color get() = com.example.ui.SoftTheme.LightPink
    val NifasRose: Color get() = com.example.ui.SoftTheme.NifasRose
    val MintTeal: Color get() = com.example.ui.SoftTheme.MintTeal
    val TextWhite: Color get() = com.example.ui.SoftTheme.TextWhite
    val SoftGray: Color get() = com.example.ui.SoftTheme.SoftGray
    val RedDanger: Color get() = com.example.ui.SoftTheme.RedDanger
    val PregnancyPurple: Color get() = com.example.ui.SoftTheme.PregnancyPurple
    val SoftPurple: Color get() = com.example.ui.SoftTheme.SoftPurple
    val WarmCoral: Color get() = com.example.ui.SoftTheme.WarmCoral
    val SoftTeal: Color get() = com.example.ui.SoftTheme.SoftTeal
    val CardBorder: Color get() = com.example.ui.SoftTheme.CardBorder
    val CanvasBg: Color get() = com.example.ui.SoftTheme.CanvasBg
    val CardBg: Color get() = com.example.ui.SoftTheme.CardBg
    val EmeraldPrimary: Color get() = com.example.ui.SoftTheme.EmeraldPrimary
    val TealDark: Color get() = com.example.ui.SoftTheme.TealDark
    val MintAccent: Color get() = com.example.ui.SoftTheme.MintAccent
    val MintAccentBorder: Color get() = com.example.ui.SoftTheme.MintAccentBorder
    val isDark: Boolean get() = com.example.ui.SoftTheme.isDark
}

object ModernHealthTokens {
    // Canvas & Surfaces
    val CanvasBackground = Color(0xFFF6FAF8)   // Soft ivory mint tinted off-white
    val CardWhite = Color(0xFFFFFFFF)          // Pure crisp white cards
    val CardBorderLight = Color(0xFFD4E6DF)    // Refined crisp mint border for distinctive cards
    val CardShadow = Color(0x12000000)

    // Primary Emerald / Teal
    val PrimaryEmerald = Color(0xFF00A884)     // Main action green/emerald
    val PrimaryTeal = Color(0xFF00897B)        // Deep healthy teal
    val EmeraldGradientEnd = Color(0xFF00897B)

    // Accent Mint
    val AccentMint = Color(0xFFE8F7F2)         // Pale mint background for badges / chips
    val AccentMintBorder = Color(0xFFC8F0E4)   // Mint border / progress track
    val AccentMintDark = Color(0xFF80CBC4)

    // Supporting category colors
    // Water
    val WaterBlue = Color(0xFF29B6F6)
    val WaterBlueDark = Color(0xFF0288D1)
    val WaterBg = Color(0xFFE1F5FE)

    // Food & Nutrition
    val FoodOrange = Color(0xFFFFA726)
    val FoodOrangeDark = Color(0xFFF57C00)
    val FoodBg = Color(0xFFFFF3E0)
    val ProteinColor = Color(0xFF4CAF50)
    val CarbColor = Color(0xFFFFA726)
    val FatColor = Color(0xFFAB47BC)

    // Sleep & Rest
    val SleepPurple = Color(0xFF5C6BC0)
    val SleepPurpleDark = Color(0xFF3949AB)
    val SleepBg = Color(0xFFEDE7F6)

    // Symptoms
    val SymptomsPurple = Color(0xFF8E24AA)
    val SymptomsBg = Color(0xFFF3E5F5)

    // Women's Health & Cycle
    val WomenPink = Color(0xFFF06292)
    val WomenPinkDark = Color(0xFFD81B60)
    val WomenPinkBg = Color(0xFFFCE4EC)

    // Jouri AI
    val JouriTurquoise = Color(0xFF26A69A)
    val JouriTurquoiseDark = Color(0xFF00897B)
    val JouriBg = Color(0xFFE0F2F1)

    // Typography & Contrast
    val TextPrimaryDark = Color(0xFF1A2E26)     // Deep rich charcoal/slate
    val TextSecondary = Color(0xFF607D8B)       // Muted slate gray
    val TextMuted = Color(0xFF90A4AE)           // Light subtle gray
}

