package cz.frank.spacex.main.di

import cz.frank.spacex.crew.di.crewModule
import cz.frank.spacex.launches.di.launchesModule
import cz.frank.spacex.main.data.SpaceXDatabase
import org.koin.dsl.module

val spaceXModule = module {
    includes(launchesModule)
    includes(crewModule)
    single { spaceXHttpClient(get()) }
    single { SpaceXDatabase.create(get()) }
}
