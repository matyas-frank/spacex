package cz.frank.spacex.sharedtests

import cz.frank.spacex.launches.data.RocketsDataSource
import cz.frank.spacex.launches.data.api.ILaunchesAPI
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import cz.frank.spacex.shared.data.PaginatedResponse

class FakeLaunchAPI : ILaunchesAPI {
    var failureMessage: FailureMessage? = null

    private val launches: MutableList<ILaunchesAPI.LaunchPreviewResponse> = mutableListOf()

    fun addLaunches(launch: ILaunchesAPI.LaunchPreviewResponse) {
        this.launches.add(launch)
    }

    fun clearLaunches() {
        launches.clear()
    }

    override suspend fun allLaunches(
        filters: ILaunchesFilterRepository.Filters,
        page: Int,
        pageSize: Int
    ): Result<PaginatedResponse<ILaunchesAPI.LaunchPreviewResponse>> {

        val maxIndex = (page*pageSize)-1
        val sliceRange = (page - 1) * pageSize..maxIndex

        failureMessage?.let {
            if (it.index in sliceRange) {
                return Result.failure(Exception(it.message))
            }
        }

        val filterRocketNames = filters.rockets.map { filterRocketId -> RocketsDataSource.rockets.find { it.id == filterRocketId }!! }.map { it.name }.toSet()

        val filtered = launches
            .filter { if (!filters.isUpcomingSelected) !it.upcoming else true }
            .filter { if (!filters.isLaunchedSelected) it.upcoming else true }
            .filter { if (filters.rockets.isNotEmpty()) it.rocket.name in filterRocketNames else true  }


        val hasNextPage = maxIndex + 1 < filtered.count()

        return Result.success(
            PaginatedResponse(
                docs = filtered.slice(sliceRange),
                page = page,
                hasNextPage = hasNextPage,
                nextPage = if (hasNextPage) page + 1 else null,
            )
        )
    }


    override suspend fun specificLaunch(id: String): Result<ILaunchesAPI.LaunchDetailResponse> {
        TODO("Not yet implemented")
    }

    data class FailureMessage(val index: Int, val message: String)
}


