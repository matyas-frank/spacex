package cz.frank.spacex.launches.di

import cz.frank.spacex.launches.data.database.dao.ILaunchesFilterDao
import cz.frank.spacex.launches.data.database.dao.LaunchesFilterDao
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRocketRepository
import cz.frank.spacex.launches.data.repository.LaunchesFilterRepository
import cz.frank.spacex.launches.data.repository.LaunchesFilterRocketRepository
import cz.frank.spacex.launches.ui.filter.LaunchFilterViewModel
import cz.frank.spacex.launches.ui.filter.rocket.LaunchFilterRocketViewModel
import cz.frank.spacex.launches.ui.search.LaunchSearchViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val launchesModule = module {
    singleOf(::LaunchesFilterDao) bind ILaunchesFilterDao::class

    singleOf(::LaunchesFilterRocketRepository) bind ILaunchesFilterRocketRepository::class
    singleOf(::LaunchesFilterRepository) bind ILaunchesFilterRepository::class

    viewModelOf(::LaunchSearchViewModel)
    viewModelOf(::LaunchFilterViewModel)
    viewModelOf(::LaunchFilterRocketViewModel)
}
