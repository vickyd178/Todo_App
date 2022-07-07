package com.doops.mvvmtodo.util

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

enum class SortOrder { BY_NAME, BY_DATE }

data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean)


@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext val context: Context) {
    private val Context.dataStore by preferencesDataStore("user_preferences")
    private val dataStore = context.dataStore
    val preferenceFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
                Log.e(TAG, "Error reading preferences", exception)
            } else
                throw exception
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferenceKeys.SORT_ORDER] ?: SortOrder.BY_NAME.name
            )
            val hideCompleted = preferences[PreferenceKeys.HIDE_COMPLETED] ?: false
            FilterPreferences(sortOrder, hideCompleted)
        }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.HIDE_COMPLETED] = hideCompleted
        }
    }


    private object PreferenceKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val HIDE_COMPLETED = booleanPreferencesKey("hide_completed")

    }
}