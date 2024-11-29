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


    suspend fun toggleLaunchedSelected()
    suspend fun toggleUpcomingSelected()
    suspend fun setQuery(query: String)
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

    override suspend fun toggleLaunchedSelected() = filterDao.toggleLaunchedSelected()
    override suspend fun toggleUpcomingSelected() = filterDao.toggleUpcomingSelected()
    override suspend fun setQuery(query: String) = filterDao.setQuery(query)
}
