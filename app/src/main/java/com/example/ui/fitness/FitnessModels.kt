package com.example.ui.fitness

// High-fidelity steps representing exercise movements
data class JouriStep(
    val title: String,
    val subtitle: String,
    val durationBadge: String? = null,
    val iconType: String // "sit", "hold", "relax", "stretch", "breathe"
)

// Data class representing an exercise
data class JouriExercise(
    val id: String,
    val name: String,
    val category: String, // "pregnancy", "postpartum", "general"
    val durationSeconds: Int,
    val emoji: String,
    val description: String,
    val goal: String,
    val stepDetails: List<JouriStep>,
    val benefits: List<String>,
    val safetyWarning: String,
    val recommendedTrimesters: Set<Int> = setOf(1, 2, 3)
)
