package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --- Energizing & Eye-Comfortable Peach & Botanical Green Palette ---

// Botanical Green Palette (Focus, Clarity & Growth)
val GreenPrimary = Color(0xFF1B8A5A)         // Vibrant, motivating emerald green
val GreenDark = Color(0xFF136843)            // Deep botanical forest green
val GreenLight = Color(0xFF34A874)           // Fresh lively mint-emerald
val GreenMint = Color(0xFFA7E3C5)            // Soft luminous mint
val GreenContainer = Color(0xFFDDF5E8)       // Airy mint container
val GreenContainerDark = Color(0xFF123B27)   // Deep container for dark mode
val OnGreen = Color(0xFFFFFFFF)              // Crisp white on vibrant green

// Sunlit Peach Palette (Warmth, Motivation & Discovery)
val PeachPrimary = Color(0xFFF27B53)         // Warm, energizing sunlit peach
val PeachLight = Color(0xFFFF9E7D)           // Soft lively coral-peach
val PeachSoft = Color(0xFFFFDFD1)            // Gentle peach tint
val PeachContainer = Color(0xFFFFEDE4)       // Warm peach card container
val PeachContainerDark = Color(0xFF4E2215)   // Deep peach container for dark mode
val OnPeach = Color(0xFFFFFFFF)              // Crisp white on vibrant peach
val OnPeachContainer = Color(0xFF7A2B12)     // Rich terracotta text on soft peach

// Warm, Fatigue-Free Daylight Surfaces (Never dull, never harsh glare)
val TraceBackground = Color(0xFFFFFBF7)       // Warm sunlit ivory-cream canvas
val TraceBackgroundPeachTint = Color(0xFFFFF2E8) // Soft sunlit peach upper glow
val TraceBackgroundMintTint = Color(0xFFF0FAF4)  // Fresh botanical mint lower calm
val TraceSurface = Color(0xFFFFFFFF)          // Clean elevated card white
val TraceSurfaceVariant = Color(0xFFF3F8F5)   // Gentle sage-tinted nested surface
val TraceSurfacePeach = Color(0xFFFFF4EE)     // Warm peach-tinted surface for highlights
val TraceSurfaceHighlight = Color(0xFFE5F2EB) // Subtle mint highlight
val TraceOutline = Color(0xFFD6E3DC)          // Soft sage-warm border
val TraceOutlineVariant = Color(0xFFE7EEEA)

// Ambient Gradient Brush for motivating, non-dull backgrounds
val TraceAmbientBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        TraceBackgroundPeachTint,
        TraceBackground,
        TraceBackgroundMintTint
    )
)

val TraceHeroGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFE5D6),
        Color(0xFFF5FBF7),
        Color(0xFFD9F3E5)
    )
)

// High-Comfort Editorial Text Hierarchy
val TraceTextPrimary = Color(0xFF162922)      // Deep forest ink (high contrast, zero harshness)
val TraceTextSecondary = Color(0xFF4A6359)    // Calm slate-sage secondary text
val TraceTextMuted = Color(0xFF71897F)        // Soft moss muted metadata

// Error & Status Accents
val TraceErrorContainer = Color(0xFFFFE6E6)
val TraceOnErrorContainer = Color(0xFF9E1C1C)
val TraceErrorBorder = Color(0xFFE57373)

// Semantic Aliases
val TraceTealPrimary = GreenPrimary
val TraceTealDark = GreenDark
val TraceTealContainer = GreenContainer
val TraceOnTeal = OnGreen
val TraceCyanSecondary = PeachPrimary
val TraceCyanContainer = PeachContainer
val TraceAmberTertiary = Color(0xFFE08E26)    // Warm golden honey accent
val TraceAmberContainer = Color(0xFFFFF3DF)
val TraceEmerald = GreenPrimary
val TraceRose = Color(0xFFE04868)
val TraceIndigo = Color(0xFF5B6AF0)
