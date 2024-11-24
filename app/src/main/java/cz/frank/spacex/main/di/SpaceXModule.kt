package cz.frank.spacex.main.di

import cz.frank.spacex.shared.data.HttpClient
import org.koin.dsl.module

val spaceXModule = module {
    single { HttpClient("api.spacexdata.com", "v4") }
}
