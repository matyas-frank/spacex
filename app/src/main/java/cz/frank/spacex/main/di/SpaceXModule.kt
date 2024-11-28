package cz.frank.spacex.main.di

import cz.frank.spacex.launches.di.launchesModule
import cz.frank.spacex.shared.data.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

val spaceXModule = module {
    includes(launchesModule)
    single { HttpClient("api.spacexdata.com", OkHttp.create()) }
}
