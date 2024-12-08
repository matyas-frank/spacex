package cz.frank.spacex

import cz.frank.spacex.launches.data.api.LaunchesAPI
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import cz.frank.spacex.shared.data.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.logging.Logger
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
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
    fun `Parse success launches response`() = runTest {
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
        val response = api.allLaunches(
            query =
                ILaunchesFilterRepository.Filters(
                    false,
                    false,
                    setOf(),
                    "",
                ),
            page = 21,
            pageSize = 10,
        )
        assertTrue(response.isSuccess)
        response.getOrNull()?.let {
            assertEquals(1, it.docs.size)
        } ?: assert(false)

    }

    @Test
    @Suppress("SwallowedException")
    fun `Parse failure 3XX request`() = runTest {
        failureRequestTest(HttpStatusCode.UseProxy) {
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
    fun `Parse failure 4XX request`() = runTest {
        failureRequestTest(HttpStatusCode.BadRequest) {
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
    fun `Parse failure 5XX request`() = runTest {
        failureRequestTest(HttpStatusCode.InternalServerError) {
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
    fun `Parse success detail launch response`() = runTest {
        val api = launchesApi(MockEngine { _ ->
            respond(
                content = """
                    {
                        "docs": [
                            {
                                "fairings": {
                                    "recovered": null
                                },
                                "links": {
                                    "youtube_id": null,
                                    "article": null
                                },
                                "rocket": {
                                    "name": "Falcon 9",
                                    "id": "5e9d0d95eda69973a809d1ec"
                                },
                                "success": null,
                                "details": null,
                                "launchpad": {
                                    "full_name": "Cape Canaveral Space Force Station Space Launch Complex 40",
                                    "id": "5e9e4501f509094ba4566f84"
                                },
                                "flight_number": 198,
                                "name": "SES-18 & SES-19",
                                "date_unix": 1667260800,
                                "upcoming": true,
                                "id": "633f72000531f07b4fdf59c2"
                            }
                        ],
                        "totalDocs": 1,
                        "offset": 0,
                        "limit": 1,
                        "totalPages": 1,
                        "page": 1,
                        "pagingCounter": 1,
                        "hasPrevPage": false,
                        "hasNextPage": false,
                        "prevPage": null,
                        "nextPage": null
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val id = "633f72000531f07b4fdf59c2"
        val response = api.specificLaunch(id)
        println(response)
        assertTrue(response.isSuccess)
        response.onSuccess {
            assertEquals(id, it.id)
        }

    }

    private fun launchesApi(engine: MockEngine) = LaunchesAPI(HttpClient("api.spacexdata.com", engine, object : Logger {
        override fun log(message: String) {
            println(message)
        }
    }))

    private suspend fun failureRequestTest(code: HttpStatusCode, check: Result<*>.() -> Boolean) {
        val api = launchesApi(failureEngine(code))
        val response = api.allLaunches(
            query = ILaunchesFilterRepository.Filters(
                false,
                false,
                setOf(),
                "",
            ), page = 1, pageSize = 2
        )
        assertTrue(response.isFailure)
        assert(response.check())
    }

    private fun failureEngine(code: HttpStatusCode) = MockEngine { _ ->
        respond(
            content = ByteReadChannel(
                ""
            ),
            status = code,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
}
