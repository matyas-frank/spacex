package cz.frank.spacex.launches.data.repository

import cz.frank.spacex.launches.data.RocketResponse
import cz.frank.spacex.launches.data.RocketsDataSource
import cz.frank.spacex.launches.data.database.dao.ILaunchesFilterDao
import cz.frank.spacex.launches.data.database.dao.IRefreshDao
import kotlinx.coroutines.flow.first

interface ILaunchesFilterRocketRepository {
    suspend fun getRockets(): List<RocketFilterModel>
    suspend fun saveRockets(ids: Set<String>)
}

class LaunchesFilterRocketRepository(
    private val filtersDao: ILaunchesFilterDao,
    private val refreshDao: IRefreshDao,
) : ILaunchesFilterRocketRepository {
    override suspend fun getRockets(): List<RocketFilterModel> {
        val selectedIds = filtersDao.selectedRocketsId.first()
        return RocketsDataSource.rockets.map {
            it.toUiModel(it.id in selectedIds)
        }
    }

    override suspend fun saveRockets(ids: Set<String>) {
        filtersDao.setRocketsIds(ids)
        refreshDao.mustBeRefresh()
    }
}

fun RocketResponse.toUiModel(isSelected: Boolean) = RocketFilterModel(id, name, isSelected)

data class RocketFilterModel(val id: String, val name: String, val isSelected: Boolean)
