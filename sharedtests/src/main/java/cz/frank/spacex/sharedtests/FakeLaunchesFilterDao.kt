package cz.frank.spacex.sharedtests

import cz.frank.spacex.launches.data.database.dao.ILaunchesFilterDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeLaunchesFilterDao : ILaunchesFilterDao {
    private val _query = MutableStateFlow("")
    override val query: Flow<String> = _query

    private val _isLaunchedSelected = MutableStateFlow(true)
    override val isLaunchedSelected: Flow<Boolean> = _isLaunchedSelected

    private val _isUpcomingSelected = MutableStateFlow(true)
    override val isUpcomingSelected: Flow<Boolean> = _isUpcomingSelected

    override val selectedRocketsId = MutableStateFlow<Set<String>>(setOf())

    override suspend fun setRocketsIds(ids: Set<String>) {
        selectedRocketsId.value = ids
    }

    override suspend fun setQuery(query: String) {
        _query.value = query
    }

    override suspend fun toggleLaunchedSelected() {
        _isLaunchedSelected.update { !it }
    }

    override suspend fun toggleUpcomingSelected() {
        _isUpcomingSelected.update { !it }
    }

    fun clearFilters() {
        selectedRocketsId.value = setOf()
        _query.value = ""
        _isLaunchedSelected.value = true
        _isUpcomingSelected.value = true
    }
}
