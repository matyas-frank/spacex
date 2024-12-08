package cz.frank.spacex.launches.data.database.dao

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface IRefreshDao {
    val timeOfLastRefreshInMillis: Flow<Long?>
    suspend fun changeTimeOfLastUpdate(millis: Long?)
    suspend fun mustBeRefresh() = changeTimeOfLastUpdate(0)
}

class RefreshDao(private val context: Context) : IRefreshDao {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "refresh")
    private val lastRefreshKey = longPreferencesKey("last_updated")
    override val timeOfLastRefreshInMillis: Flow<Long?> = context.dataStore.data.map { it[lastRefreshKey] }

    override suspend fun changeTimeOfLastUpdate(millis: Long?) {
        context.dataStore.edit { settings ->
            settings[lastRefreshKey] = millis ?: 0
        }
    }
}
