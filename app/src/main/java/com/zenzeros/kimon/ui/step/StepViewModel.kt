package com.zenzeros.kimon.ui.step

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zenzeros.kimon.data.repository.UserSettingsRepository
import com.zenzeros.kimon.service.step.StepCounterManager
import com.zenzeros.kimon.service.step.StepCounterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StepUiState(
    val isEnabled: Boolean = false,
    val todaySteps: Int = 0,
    val dailyGoal: Int = 8000,
    val isSensorAvailable: Boolean = true,
    val hasPermission: Boolean = false,
    val distanceKm: Float = 0f,
    val caloriesKcal: Int = 0
)

class StepViewModel(
    private val stepCounterManager: StepCounterManager,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _hasPermission = MutableStateFlow(stepCounterManager.hasPermission())

    val uiState: StateFlow<StepUiState> = combine(
        stepCounterManager.todaySteps,
        userSettingsRepository.dailyStepGoal,
        userSettingsRepository.stepCounterEnabled,
        _hasPermission
    ) { steps, goal, enabled, hasPerm ->
        StepUiState(
            isEnabled = enabled,
            todaySteps = steps,
            dailyGoal = goal,
            isSensorAvailable = stepCounterManager.isSensorAvailable(),
            hasPermission = hasPerm,
            distanceKm = StepCounterManager.calculateDistanceKm(steps),
            caloriesKcal = StepCounterManager.calculateCaloriesKcal(steps)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StepUiState())

    fun onPermissionResult(granted: Boolean) {
        _hasPermission.value = granted
        if (granted) {
            StepCounterService.start(stepCounterManager.context)
        }
    }

    fun updateGoal(newGoal: Int) {
        viewModelScope.launch {
            userSettingsRepository.setDailyStepGoal(newGoal)
        }
    }

    companion object {
        fun Factory(
            stepCounterManager: StepCounterManager,
            userSettingsRepository: UserSettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StepViewModel(stepCounterManager, userSettingsRepository) as T
            }
        }
    }
}
