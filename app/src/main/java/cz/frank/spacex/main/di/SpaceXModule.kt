package cz.frank.spacex.main.di

import cz.frank.spacex.launches.di.launchesModule
import cz.frank.spacex.shared.data.HttpClient
import org.koin.dsl.module

val spaceXModule = module {
    includes(launchesModule)
    single { HttpClient("api.spacexdata.com", "v4") }
}
