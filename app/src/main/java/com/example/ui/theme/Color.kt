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
    val BlossomCream = Color(0xFFFDF8F9)   // Ultra soft, warm rose-tinted off-white
    val PureWhite = Color(0xFFFFFFFF)      // Pure card surfaces
    val SoftAlabaster = Color(0xFFF1F3F6)  // Sub-cards and inputs
    val SlateBlueLight = Color(0xFF1A237E) // Rich, high-contrast text color
    val SoftRoseLight = Color(0xFFFFD0DC)  // Delicate background highlights
    val PassionRoseLight = Color(0xFFE84E75) // Radiant maternal pink
    val MagentaPinkLight = Color(0xFFD81B60) // Deep rich primary accent
    val ForestTealLight = Color(0xFF00897B) // Natural healing teal
    val EmeraldTealLight = Color(0xFF00BFA5) // Bright active state green
    
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
