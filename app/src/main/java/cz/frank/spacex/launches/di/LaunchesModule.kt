package cz.frank.spacex.launches.di

import cz.frank.spacex.launches.ui.search.LaunchSearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val launchesModule = module {
    viewModelOf(::LaunchSearchViewModel)
}
