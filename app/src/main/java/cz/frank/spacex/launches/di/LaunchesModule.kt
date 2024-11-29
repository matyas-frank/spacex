package cz.frank.spacex.launches.di

import cz.frank.spacex.launches.data.database.dao.ILaunchesFilterDao
import cz.frank.spacex.launches.data.database.dao.LaunchesFilterDao
import cz.frank.spacex.launches.ui.search.LaunchSearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val launchesModule = module {
    singleOf(::LaunchesFilterDao) bind ILaunchesFilterDao::class
    viewModelOf(::LaunchSearchViewModel)
}
