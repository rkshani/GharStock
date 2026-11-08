package com.rk.gharstock.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    /** True once the app has asked for the notification permission at startup. */
    val notificationPromptShown: Flow<Boolean> =
        dataStore.data.map { it[NOTIFICATION_PROMPT_SHOWN] ?: false }

    suspend fun setNotificationPromptShown() {
        dataStore.edit { it[NOTIFICATION_PROMPT_SHOWN] = true }
    }

    private companion object {
        val NOTIFICATION_PROMPT_SHOWN = booleanPreferencesKey("notification_prompt_shown")
    }
}
