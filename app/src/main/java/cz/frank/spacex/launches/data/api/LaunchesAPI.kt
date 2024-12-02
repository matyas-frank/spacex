package cz.frank.spacex.launches.data.api

import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import cz.frank.spacex.shared.data.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

interface ILaunchesAPI {
    suspend fun allLaunches(
        query: ILaunchesFilterRepository.Filters,
        limit: Int,
        page: Int
    ): Result<PaginatedResponse<LaunchPreviewResponse>>
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
    override suspend fun allLaunches(query: ILaunchesFilterRepository.Filters, limit: Int, page: Int) =
        runCatching<PaginatedResponse<ILaunchesAPI.LaunchPreviewResponse>> {
            httpClient.post("$BASE_LAUNCHES_URL/query") {
                setBody(
                    RequestBody(
                        query.toJson(),
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

    private fun ILaunchesFilterRepository.Filters.toJson() = buildJsonObject {
        if (!(isUpcomingSelected && isLaunchedSelected)) {
            put("upcoming", isUpcomingSelected)
        }
        if (rocketsCount.isNotEmpty()) {
            put("rocket", buildJsonObject {
                putJsonArray("\$in") {
                    rocketsCount.forEach { this.add(it) }
                }
            })
        }
        if (query.isNotBlank()) {
            put("name", buildJsonObject {
                put("\$regex", query)
                put("\$options", "i")
            })
        }
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
