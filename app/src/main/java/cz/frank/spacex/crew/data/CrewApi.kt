package cz.frank.spacex.crew.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

interface ICrewApi {
    suspend fun crewMembers(): Result<List<CrewMemberResponse>>

    @Serializable
    data class CrewMemberResponse(val name: String, val status: String, val image: String, val wikipedia: String)
}

class CrewApi(private val httpClient: HttpClient) : ICrewApi {
    override suspend fun crewMembers(): Result<List<ICrewApi.CrewMemberResponse>> =
        runCatching { httpClient.get(BASE_CREW_URL).body() }

    companion object {
        private const val BASE_CREW_URL = "v4/crew"
    }
}

