package cz.frank.spacex.sharedtests

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import cz.frank.spacex.launches.data.RocketsDataSource
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import cz.frank.spacex.launches.data.repository.ILaunchesRepository
import cz.frank.spacex.launches.domain.model.LaunchDetailModel
import cz.frank.spacex.launches.domain.model.LaunchPreviewModel
import kotlinx.coroutines.flow.Flow

class FakeLaunchesRepository : ILaunchesRepository {
    var launches = listOf<LaunchPreviewModel>()
        private set

    fun setLaunches(launches: List<LaunchPreviewModel>) { this.launches = launches  }
    fun clearLaunches() { this.launches = listOf() }

    override fun pager(filters: ILaunchesFilterRepository.Filters): Flow<PagingData<LaunchPreviewModel>> {
        val filterRocketNames = filters.rockets.map { filterRocketId ->
            RocketsDataSource.rockets.find { it.id == filterRocketId }!!
        }.map { it.name }.toSet()

        val filtered = launches
            .filter { if (!filters.isUpcomingSelected) it.state !is LaunchPreviewModel.State.Upcoming else true }
            .filter { if (!filters.isLaunchedSelected) it.state is LaunchPreviewModel.State.Upcoming else true }
            .filter { if (filters.rockets.isNotEmpty()) it.rocket in filterRocketNames else true  }
            .filter { if (filters.query.isNotBlank()) it.title.contains(filters.query) else true }


        return Pager(PagingConfig(10), pagingSourceFactory = filtered.asPagingSourceFactory()).flow
    }

    override suspend fun detailLaunch(id: String): Result<LaunchDetailModel> {
        TODO("Not yet implemented")
    }
}
