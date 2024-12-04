package cz.frank.spacex.launches.data.repository

import cz.frank.spacex.launches.data.database.dao.ILaunchesFilterDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

interface ILaunchesFilterRepository {
    val isUpcomingSelected: Flow<Boolean>
    val isLaunchedSelected: Flow<Boolean>
    val rocketsCount: Flow<Int>
    val isAnyFilterActive: Flow<Boolean>
    val query: Flow<String>
    val allFilters: Flow<Filters>
    val timeOfLastRefreshInMillis: Flow<Long?>


    suspend fun toggleLaunchedSelected()
    suspend fun toggleUpcomingSelected()
    suspend fun setQuery(query: String)
    suspend fun changeLastRefresh(millis: Long)

    data class Filters(
        val isUpcomingSelected: Boolean,
        val isLaunchedSelected: Boolean,
        val rocketsCount: Set<String>,
        val query: String,
    )
}

class LaunchesFilterRepository(private val filterDao: ILaunchesFilterDao) : ILaunchesFilterRepository {
    override val isLaunchedSelected: Flow<Boolean> = filterDao.isLaunchedSelected
    override val isUpcomingSelected: Flow<Boolean> = filterDao.isUpcomingSelected
    override val query: Flow<String> = filterDao.query
    override val rocketsCount = filterDao.selectedRocketsId.map { it.count() }
    override val isAnyFilterActive = combine(
        filterDao.isLaunchedSelected,
        filterDao.isUpcomingSelected,
        filterDao.selectedRocketsId
    ) { launched, upcoming, rockets ->
        !launched || !upcoming || rockets.isNotEmpty()
    }
    override val timeOfLastRefreshInMillis: Flow<Long?> = filterDao.timeOfLastRefresh


    override suspend fun toggleLaunchedSelected() = updateTime { filterDao.toggleLaunchedSelected() }
    override suspend fun toggleUpcomingSelected() = updateTime { filterDao.toggleUpcomingSelected() }
    override suspend fun setQuery(query: String) = updateTime { filterDao.setQuery(query) }

    override val allFilters: Flow<ILaunchesFilterRepository.Filters> = combine(
        isUpcomingSelected,
        isLaunchedSelected,
        filterDao.selectedRocketsId,
        query,
    ) { upcoming, launched, selectedRockets, query ->
        ILaunchesFilterRepository.Filters(upcoming, launched, selectedRockets, query)
    }
    private suspend fun updateTime(block: suspend () -> Unit) {
        filterDao.changeLastUpdated(null)
        block()
    }

    override suspend fun changeLastRefresh(millis: Long) {
        filterDao.changeLastUpdated(millis)
    }
}
