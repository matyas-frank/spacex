package cz.frank.spacex

import cz.frank.spacex.launches.data.LaunchesAPI
import cz.frank.spacex.shared.data.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class LaunchApiTest {
    @Test
    fun successResponse() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(
                    """
                        {
                            "docs": [
                                {
                                    "links": {
                                        "patch": {
                                            "small": null
                                        }
                                    },
                                    "rocket": {
                                        "name": "Falcon 9",
                                        "id": "5e9d0d95eda69973a809d1ec"
                                    },
                                    "success": null,
                                    "name": "SES-18 & SES-19",
                                    "upcoming": true,
                                    "id": "633f72000531f07b4fdf59c2"
                                }
                            ],
                            "totalDocs": 2,
                            "limit": 1,
                            "totalPages": 2,
                            "page": 1,
                            "pagingCounter": 1,
                            "hasPrevPage": false,
                            "hasNextPage": true,
                            "prevPage": null,
                            "nextPage": 2
                        }
                    """.trimIndent()
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val api = launchesApi(engine)
        val response = api.allLaunches(query = buildJsonObject {  }, limit = 10, page = 21)
        assertTrue(response.isSuccess)
        response.getOrNull()?.let {
            assertEquals(1, it.docs.size)
        } ?: assert(false)

    }

    @Test
    @Suppress("SwallowedException")
    fun failureRedirectResponse() = runTest {
        failureRequest(HttpStatusCode.UseProxy) {
            try {
                getOrThrow()
                false
            }
            catch (e: RedirectResponseException) {
                true
            }
        }
    }

    @Test
    @Suppress("SwallowedException")
    fun failureClientRequest() = runTest {
        failureRequest(HttpStatusCode.BadRequest) {
            try {
                getOrThrow()
                false
            }
            catch (e: ClientRequestException) {
                true
            }
        }
    }

    @Test
    @Suppress("SwallowedException")
    fun failureServerResponse() = runTest {
        failureRequest(HttpStatusCode.InternalServerError) {
            try {
                getOrThrow()
                false
            }
            catch (e: ServerResponseException) {
                true
            }
        }
    }

    @Test
    fun detailRequest() = runTest {
        val api = LaunchesAPI(HttpClient("api.spacexdata.com", OkHttp.create()))
        val response = api.specificLaunch("633f72000531f07b4fdf59c2")
        println(response)
        assertTrue(response.isSuccess)
    }

    private fun launchesApi(engine: MockEngine) = LaunchesAPI(HttpClient("api.spacexdata.com", engine))

    private fun failureEngine(code: HttpStatusCode) = MockEngine { _ ->
        respond(
            content = ByteReadChannel(
                ""
            ),
            status = code,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }

    private suspend fun failureRequest(code: HttpStatusCode, check: Result<*>.() -> Boolean) {
        val api = launchesApi(failureEngine(code))
        val response = api.allLaunches(query = buildJsonObject {  }, limit = 10, page = 21)
        assertTrue(response.isFailure)
        assert(response.check())
    }
}
