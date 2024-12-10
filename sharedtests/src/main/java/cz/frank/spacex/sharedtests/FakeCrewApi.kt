package cz.frank.spacex.sharedtests

import cz.frank.spacex.crew.data.ICrewApi

class FakeCrewApi : ICrewApi {
    override suspend fun crewMembers(): Result<List<ICrewApi.CrewMemberResponse>> {
        return Result.success(listOf())
    }
}
