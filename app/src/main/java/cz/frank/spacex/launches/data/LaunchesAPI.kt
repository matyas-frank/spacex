package cz.frank.spacex.launches.data

import cz.frank.spacex.launches.data.LaunchDetailResponse.Links
import cz.frank.spacex.launches.data.LaunchDetailResponse.Rocket
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface ILaunchesAPI {
    suspend fun allLaunches(query: JsonElement, limit: Int, page: Int): Result<PaginatedResponse<LaunchPreviewResponse>>
    suspend fun specificLaunch(id: String): Result<LaunchDetailResponse>
}

class LaunchesAPI(private val httpClient: HttpClient) : ILaunchesAPI {
    override suspend fun allLaunches(query: JsonElement, limit: Int, page: Int) =
        runCatching<PaginatedResponse<LaunchPreviewResponse>> {
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
        runCatching<LaunchDetailResponse> {
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
            }.body<PaginatedResponse<LaunchDetailResponse>>().docs.first()
        }

    interface SelectionBuilder {
        fun select(property: String)
    }

    private fun rocketPopulation(selection: SelectionBuilder.() -> Unit) = buildJsonObject {
        put("path", "rocket")
        putSelection(selection)
    }
    private fun JsonObjectBuilder.putSelection(selection: SelectionBuilder.() -> Unit) {
        put("select", buildSelection(selection))
    }

    private fun buildSelection(selection: SelectionBuilder.() -> Unit) = buildJsonObject {
        object : SelectionBuilder {
            override fun select(property: String) {
                put(property, 1)
            }
        }.selection()
    }

    companion object {
        private const val BASE_LAUNCHES_URL = "v5/launches"
    }
}

@Serializable data class LaunchPreviewResponse(
    val id: String,
    val name: String,
    val links: Links,
    val rocket: Rocket,
    val upcoming: Boolean,
    val success: Boolean?,
)

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

@Serializable data class RequestBody(val query: JsonElement, val options: RequestOptions)

@Serializable data class RequestOptions(
    val select: JsonElement,
    val limit: Int = 10,
    val page: Int = 21,
    val populate: List<JsonElement> = listOf()
)

@Serializable data class PaginatedResponse<T>(
    val docs: List<T>,
    val page: Int,
    val hasPrevPage: Boolean,
    val hasNextPage: Boolean,
    val prevPage: Int?,
    val nextPage: Int?
)
