package cz.frank.spacex.shared.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val HUNDRED_SECONDS = 100_000

@Suppress("FunctionNaming")
fun KtorClient(host: String, path: String) = HttpClient(Android) {
    expectSuccess = true


    engine {
        connectTimeout = HUNDRED_SECONDS
    }

    install(Logging)

    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 5)
        exponentialDelay()
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
    }

    defaultRequest {
        url {
            protocol = URLProtocol.HTTPS
            this.host = host
            path(path)
        }
    }
}
