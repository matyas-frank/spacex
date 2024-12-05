package cz.frank.spacex.crew.data

import cz.frank.spacex.crew.domain.model.CrewMemberModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CrewRepository(private val crewApi: ICrewApi) {
    suspend fun fetchCrew(): Result<List<CrewMemberModel>> {
        return withContext(Dispatchers.IO) {
            crewApi.crewMembers().mapCatching { it.map { it.toModel() } }
        }
    }
}

private fun ICrewApi.CrewMemberResponse.toModel() = CrewMemberModel(name, status.toStatus(), image, wikipedia)
private fun String.toStatus() = when (this) {
    "active" -> CrewMemberModel.Status.ACTIVE
    "inactive" -> CrewMemberModel.Status.INACTIVE
    "retired" -> CrewMemberModel.Status.RETIRED
    else -> CrewMemberModel.Status.UNKNOWN
}
