package cz.frank.spacex

import cz.frank.spacex.launches.data.LaunchesAPI
import cz.frank.spacex.launches.data.RequestBody
import cz.frank.spacex.launches.data.RequestOptions
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
                                    "fairings": {
                                        "reused": null,
                                        "recovery_attempt": null,
                                        "recovered": null,
                                        "ships": []
                                    },
                                    "links": {
                                        "patch": {
                                            "small": null,
                                            "large": null
                                        },
                                        "reddit": {
                                            "campaign": null,
                                            "launch": null,
                                            "media": null,
                                            "recovery": null
                                        },
                                        "flickr": {
                                            "small": [],
                                            "original": []
                                        },
                                        "presskit": null,
                                        "webcast": null,
                                        "youtube_id": null,
                                        "article": null,
                                        "wikipedia": null
                                    },
                                    "static_fire_date_utc": null,
                                    "static_fire_date_unix": null,
                                    "net": false,
                                    "window": null,
                                    "rocket": "5e9d0d95eda69973a809d1ec",
                                    "success": null,
                                    "failures": [],
                                    "details": null,
                                    "crew": [],
                                    "ships": [],
                                    "capsules": [],
                                    "payloads": [],
                                    "launchpad": "5e9e4501f509094ba4566f84",
                                    "flight_number": 198,
                                    "name": "SES-18 & SES-19",
                                    "date_utc": "2022-11-01T00:00:00.000Z",
                                    "date_unix": 1667260800,
                                    "date_local": "2022-10-31T20:00:00-04:00",
                                    "date_precision": "month",
                                    "upcoming": true,
                                    "cores": [
                                        {
                                            "core": null,
                                            "flight": null,
                                            "gridfins": true,
                                            "legs": true,
                                            "reused": false,
                                            "landing_attempt": null,
                                            "landing_success": null,
                                            "landing_type": null,
                                            "landpad": null
                                        }
                                    ],
                                    "auto_update": true,
                                    "tbd": false,
                                    "launch_library_id": null,
                                    "id": "633f72000531f07b4fdf59c2"
                                }
                            ],
                            "totalDocs": 205,
                            "limit": 10,
                            "totalPages": 21,
                            "page": 21,
                            "pagingCounter": 201,
                            "hasPrevPage": true,
                            "hasNextPage": false,
                            "prevPage": 20,
                            "nextPage": null
                        }
                    """.trimIndent()
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val api = launchesApi(engine)
        val body = RequestBody(buildJsonObject {  }, RequestOptions(page = 21))
        val response = api.allLaunches(body)
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
        val body = RequestBody(buildJsonObject {  }, RequestOptions())
        val response = api.allLaunches(body)
        assertTrue(response.isFailure)
        assert(response.check())
    }
}
