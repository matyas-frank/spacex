package cz.frank.spacex.launches.data.database.dao

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface IRemoteKeyDao {
    val nextPageToLoad: Flow<Int>
    val defaultPageToLoad: Int
    suspend fun updateNextPage(page: Int)

}

class RemoteKeyDao(private val context: Context) : IRemoteKeyDao {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launch_remote")
    private val pageKey = intPreferencesKey("page")
    override val nextPageToLoad: Flow<Int> = context.dataStore.data.map { it[pageKey] ?: DEFAULT_PAGE_TO_LOAD }
    override val defaultPageToLoad: Int = DEFAULT_PAGE_TO_LOAD

    override suspend fun updateNextPage(page: Int) {
        context.dataStore.edit { settings ->
            settings[pageKey] = page
        }
    }

    companion object {
        private const val DEFAULT_PAGE_TO_LOAD = 1
    }
}
