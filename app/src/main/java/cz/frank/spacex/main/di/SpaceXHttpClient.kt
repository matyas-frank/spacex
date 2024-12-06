package cz.frank.spacex.main.di

import android.content.Context
import cz.frank.spacex.shared.data.HttpClient
import cz.frank.spacex.shared.util.KtorAndroidLogger
import io.ktor.client.engine.okhttp.OkHttp

fun spaceXHttpClient(context: Context? = null) =
    HttpClient(
        host = "api.spacexdata.com",
        engine = OkHttp.create(),
        logger = KtorAndroidLogger(),
        context = context
    )
