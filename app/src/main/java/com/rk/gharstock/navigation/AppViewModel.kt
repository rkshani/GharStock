package com.rk.gharstock.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rk.gharstock.core.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUiState(val askNotificationPermission: Boolean = false)

sealed interface AppEvent {
    data object NotificationPromptShown : AppEvent
}

/** App-wide state: first-launch notification permission prompt. */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferences: AppPreferences,
) : ViewModel() {

    val uiState: StateFlow<AppUiState> = preferences.notificationPromptShown
        .map { shown -> AppUiState(askNotificationPermission = !shown) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    fun onEvent(event: AppEvent) {
        when (event) {
            AppEvent.NotificationPromptShown -> viewModelScope.launch { preferences.setNotificationPromptShown() }
        }
    }
}
