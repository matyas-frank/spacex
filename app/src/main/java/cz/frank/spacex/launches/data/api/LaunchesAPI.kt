package cz.frank.spacex.launches.data.api

import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import cz.frank.spacex.shared.data.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

interface ILaunchesAPI {
    suspend fun allLaunches(
        filters: ILaunchesFilterRepository.Filters,
        page: Int,
        pageSize: Int,
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
        val launchpad: Launchpad?,
        val upcoming: Boolean,
        val success: Boolean?,
        val details: String?,
        @SerialName("date_unix") val date: Long,
        @SerialName("flight_number") val flightNumber: Int,
        val fairings: Fairings,
    ) {
        @Serializable
        data class Rocket(val name: String)

        @Serializable
        data class Launchpad(@SerialName("full_name") val name: String)

        @Serializable
        data class Links(@SerialName("youtube_id") val youtubeId: String?, val article: String?)

        @Serializable
        data class Fairings(val recovered: Boolean?)
    }
}

class LaunchesAPI(private val httpClient: HttpClient) : ILaunchesAPI {
    override suspend fun allLaunches(filters: ILaunchesFilterRepository.Filters, page: Int, pageSize: Int) =
        runCatching<PaginatedResponse<ILaunchesAPI.LaunchPreviewResponse>> {
            httpClient.post("$BASE_LAUNCHES_URL/query") {
                setBody(
                    RequestBody(
                        filters.toJson(),
                        RequestOptions(
                            select = buildSelection {
                                select("name")
                                select("links.patch.small")
                                select("success")
                                select("upcoming")
                            },
                            sort = buildJsonObject {},
                            page = page,
                            limit = pageSize,
                            populate = listOf(
                                population("rocket") {
                                    select("name")
                                },
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
                        buildJsonObject { put("_id", id) },
                        RequestOptions(
                            select = buildSelection {
                                select("name")
                                select("links.article")
                                select("success")
                                select("upcoming")
                                select("flight_number")
                                select("links.youtube_id")
                                select("date_unix")
                                select("details")
                                select("fairings.recovered")
                            },
                            page = 1,
                            limit = 1,
                            populate = listOf(
                                population("rocket") {
                                    select("name")
                                },
                                population("launchpad") {
                                    select("full_name")
                                }
                            )
                        )
                    )
                )
            }.body<PaginatedResponse<ILaunchesAPI.LaunchDetailResponse>>().docs.first()
        }



    private companion object {
        const val BASE_LAUNCHES_URL = "v5/launches"
    }
}

private fun ILaunchesFilterRepository.Filters.toJson() = buildJsonObject {
    if (!(isUpcomingSelected && isLaunchedSelected)) {
        put("upcoming", isUpcomingSelected)
    }
    if (rockets.isNotEmpty()) {
        put("rocket", buildJsonObject {
            putJsonArray("\$in") {
                rockets.forEach { this.add(it) }
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
