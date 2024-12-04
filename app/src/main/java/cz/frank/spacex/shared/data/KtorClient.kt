package cz.frank.spacex.shared.data

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val HUNDRED_SECONDS = 100_000L

@Suppress("FunctionNaming")
fun HttpClient(host: String, engine: HttpClientEngine, logger: Logger) = HttpClient(engine) {
    expectSuccess = true

    install(HttpTimeout) {
        connectTimeoutMillis = HUNDRED_SECONDS
    }

    install(Logging) {
        level = LogLevel.ALL
        this.logger = logger
    }

    install(ContentNegotiation) {
        json(
            Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            },
        )
    }

    defaultRequest {
        url {
            protocol = URLProtocol.HTTPS
            this.host = host
        }
        contentType(ContentType.Application.Json)
    }
}
