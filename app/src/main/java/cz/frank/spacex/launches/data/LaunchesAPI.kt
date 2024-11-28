package cz.frank.spacex.launches.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class LaunchesAPI(private val httpClient: HttpClient) {
    suspend fun allLaunches(body: RequestBody) = runCatching<PaginatedResponse<LaunchPreviewResponse>> {
        httpClient.post("$BASE_PATH/query") {
            setBody(body)
        }.body()
    }

    suspend fun specificLaunch(id: String) =
        runCatching<LaunchDetailResponse> {
            httpClient.post("$BASE_PATH/query") {
                setBody(RequestBody(
                    buildJsonObject {
                        put("id", id)
                    }, RequestOptions(limit = 1, page = 1)
                ))
            }.body<PaginatedResponse<LaunchDetailResponse>>().docs.first()
        }

    companion object {
        private const val BASE_PATH = "v5/launches"
    }
}

@Serializable data class LaunchPreviewResponse(
    val id: String,
    val name: String,
)

@Serializable data class LaunchDetailResponse(
    val id: String,
    val name: String,
)

@Serializable data class RequestBody(val query: JsonElement, val options: RequestOptions)

@Serializable data class RequestOptions(
    val limit: Int = 10,
    val page: Int = 21
)

@Serializable data class PaginatedResponse<T>(
    val docs: List<T>,
    val page: Int,
    val hasPrevPage: Boolean,
    val hasNextPage: Boolean,
    val prevPage: Int?,
    val nextPage: Int?
)
