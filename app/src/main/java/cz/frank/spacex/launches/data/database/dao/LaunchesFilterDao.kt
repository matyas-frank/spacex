package cz.frank.spacex.launches.data.database.dao

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ILaunchesFilterDao {
    val isUpcomingSelected: Flow<Boolean>
    val isLaunchedSelected: Flow<Boolean>
    val selectedRocketsId: Flow<Set<String>>
    val query: Flow<String>

    suspend fun toggleLaunchedSelected()
    suspend fun toggleUpcomingSelected()
    suspend fun setQuery(query: String)
    suspend fun setRocketsIds(ids: Set<String>)
}

class LaunchesFilterDao(private val context: Context) : ILaunchesFilterDao {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launch_filters")
    private val isLaunchedSelectedKey = booleanPreferencesKey("is_launched_selected")
    private val isUpcomingSelectedKey = booleanPreferencesKey("is_upcoming_selected")
    private val selectedRocketIdsKey = stringSetPreferencesKey("selected_rocket_ids")
    private val queryKey = stringPreferencesKey("query")

    override val isLaunchedSelected: Flow<Boolean> =
        context.dataStore.data.map { it[isLaunchedSelectedKey] ?: IS_LAUNCHED_SELECTED_DEFAULT_VALUE }

    override val isUpcomingSelected: Flow<Boolean> =
        context.dataStore.data.map { it[isUpcomingSelectedKey] ?: IS_UPCOMING_SELECTED_DEFAULT_VALUE }

    override val selectedRocketsId: Flow<Set<String>> =
        context.dataStore.data.map { it[selectedRocketIdsKey] ?: setOf() }

    override val query: Flow<String> =
        context.dataStore.data.map { it[queryKey] ?: "" }

    override suspend fun toggleLaunchedSelected() {
        context.dataStore.edit { settings ->
            settings[isLaunchedSelectedKey] = !(settings[isLaunchedSelectedKey] ?: IS_LAUNCHED_SELECTED_DEFAULT_VALUE)
        }
    }

    override suspend fun toggleUpcomingSelected() {
        context.dataStore.edit { settings ->
            settings[isUpcomingSelectedKey] = !(settings[isUpcomingSelectedKey] ?: IS_UPCOMING_SELECTED_DEFAULT_VALUE)
        }
    }

    override suspend fun setRocketsIds(ids: Set<String>) {
        context.dataStore.edit { settings ->
            settings[selectedRocketIdsKey] = ids
        }
    }

    override suspend fun setQuery(query: String) {
        context.dataStore.edit { settings ->
            settings[queryKey] = query
        }
    }

    companion object {
        private const val IS_LAUNCHED_SELECTED_DEFAULT_VALUE = true
        private const val IS_UPCOMING_SELECTED_DEFAULT_VALUE = true
    }
}
