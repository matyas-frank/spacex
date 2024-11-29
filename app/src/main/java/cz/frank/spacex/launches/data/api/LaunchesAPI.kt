package cz.frank.spacex.launches.data.api

import cz.frank.spacex.shared.data.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface ILaunchesAPI {
    suspend fun allLaunches(query: JsonElement, limit: Int, page: Int): Result<PaginatedResponse<LaunchPreviewResponse>>
    suspend fun specificLaunch(id: String): Result<LaunchDetailResponse>

    @Serializable data class LaunchPreviewResponse(
        val id: String,
        val name: String,
        val links: Links,
        val rocket: Rocket,
        val upcoming: Boolean,
        val success: Boolean?,
    ) {
        @Serializable
        data class Rocket(val name: String)

        @Serializable
        data class Links(val patch: Patch) {
            @Serializable
            data class Patch(val small: String?)
        }
    }

    @Serializable data class LaunchDetailResponse(
        val id: String,
        val name: String,
        val links: Links,
        val rocket: Rocket,
        val upcoming: Boolean,
        val success: Boolean?,
    ) {
        @Serializable
        data class Rocket(val name: String)

        @Serializable
        data class Links(val patch: Patch) {
            @Serializable
            data class Patch(val small: String?)
        }
    }
}

class LaunchesAPI(private val httpClient: HttpClient) : ILaunchesAPI {
    override suspend fun allLaunches(query: JsonElement, limit: Int, page: Int) =
        runCatching<PaginatedResponse<ILaunchesAPI.LaunchPreviewResponse>> {
            httpClient.post("$BASE_LAUNCHES_URL/query") {
                setBody(
                    RequestBody(
                        query,
                        RequestOptions(
                            select = buildSelection {
                                select("name")
                                select("links.patch.small")
                                select("success")
                                select("upcoming")
                            },
                            limit = limit,
                            page = page,
                            populate = listOf(
                                rocketPopulation {
                                    select("name")
                                }
                            )
                        )
                    )
                )
            }.body()
        }

    override suspend fun specificLaunch(id: String) =
        runCatching<ILaunchesAPI.LaunchDetailResponse> {
            httpClient.post("$BASE_LAUNCHES_URL/query") {
                setBody(
                    RequestBody(
                        buildJsonObject { put("id", id) },
                        RequestOptions(
                            select = buildSelection {
                                select("name")
                                select("links.patch.small")
                                select("success")
                                select("upcoming")
                            },
                            limit = 1,
                            page = 1,
                            populate = listOf(
                                rocketPopulation {
                                    select("name")
                                }
                            )
                        )
                    )
                )
            }.body<PaginatedResponse<ILaunchesAPI.LaunchDetailResponse>>().docs.first()
        }

    private fun rocketPopulation(selection: SelectionBuilder.() -> Unit) = buildJsonObject {
        put("path", "rocket")
        putSelection(selection)
    }

    companion object {
        private const val BASE_LAUNCHES_URL = "v5/launches"
    }
}
