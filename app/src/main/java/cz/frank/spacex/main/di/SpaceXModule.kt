package cz.frank.spacex.main.di

import android.util.Log
import cz.frank.spacex.crew.di.crewModule
import cz.frank.spacex.launches.di.launchesModule
import cz.frank.spacex.main.data.spaceXDatabaseConstruction
import cz.frank.spacex.shared.data.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.Logger
import org.koin.dsl.module

val spaceXModule = module {
    includes(launchesModule)
    includes(crewModule)
    single {
        HttpClient(
            "api.spacexdata.com", OkHttp.create(), object : Logger {
                override fun log(message: String) {
                    Log.d("Ktor", message)
                }
            },
            get()
        )
    }
    single { spaceXDatabaseConstruction(get()) }
}
