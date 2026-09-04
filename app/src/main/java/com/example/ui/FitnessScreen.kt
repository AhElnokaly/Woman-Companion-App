package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.fitness.*
import com.example.viewmodel.WomanCompanionViewModel

// ==============================================================================
// Modular Facade for Fitness & Pedometer Subsystems
// Sub-screens have been modularized into package com.example.ui.fitness.*:
// - FitnessModels.kt (JouriStep, JouriExercise)
// - FitnessDrawings.kt (LargePelvisAnatomicalDrawing, JouriMaternalGearTracker, ExerciseStepIllustration)
// - JouriWorkoutTimerDialog.kt (Interactive countdown timer & breathing coach)
// - StepPedometerDashboard.kt (Realtime step counter, distance, calories & 7-day analytics)
// - FitnessScreen.kt (Main container & exercise selector)
// ==============================================================================

@Composable
fun FitnessScreen(viewModel: WomanCompanionViewModel) {
    com.example.ui.fitness.FitnessScreen(viewModel = viewModel)
}

@Composable
fun LargePelvisAnatomicalDrawing(modifier: Modifier = Modifier) {
    com.example.ui.fitness.LargePelvisAnatomicalDrawing(modifier = modifier)
}

@Composable
fun JouriMaternalGearTracker(completed: Boolean, modifier: Modifier = Modifier) {
    com.example.ui.fitness.JouriMaternalGearTracker(completed = completed, modifier = modifier)
}

@Composable
fun ExerciseStepIllustration(type: String, modifier: Modifier = Modifier) {
    com.example.ui.fitness.ExerciseStepIllustration(type = type, modifier = modifier)
}

@Composable
fun JouriWorkoutTimerDialog(
    exercise: JouriExercise,
    onDismiss: () -> Unit,
    onWorkoutCompleted: () -> Unit
) {
    com.example.ui.fitness.JouriWorkoutTimerDialog(
        exercise = exercise,
        onDismiss = onDismiss,
        onWorkoutCompleted = onWorkoutCompleted
    )
}

@Composable
fun StepPedometerDashboard(viewModel: WomanCompanionViewModel) {
    com.example.ui.fitness.StepPedometerDashboard(viewModel = viewModel)
}
