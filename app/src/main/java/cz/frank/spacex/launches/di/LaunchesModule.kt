package cz.frank.spacex.launches.di

import cz.frank.spacex.launches.data.api.ILaunchesAPI
import cz.frank.spacex.launches.data.api.LaunchesAPI
import cz.frank.spacex.launches.data.database.dao.*
import cz.frank.spacex.launches.data.repository.*
import cz.frank.spacex.launches.ui.filter.LaunchFilterViewModel
import cz.frank.spacex.launches.ui.filter.rocket.LaunchFilterRocketViewModel
import cz.frank.spacex.launches.ui.search.LaunchSearchViewModel
import cz.frank.spacex.main.data.SpaceXDatabase
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val launchesModule = module {
    single { get<SpaceXDatabase>().launchesDao() }
    singleOf(::RemoteKeyDao) bind IRemoteKeyDao::class
    singleOf(::RefreshDao) bind IRefreshDao::class
    singleOf(::LaunchesAPI) bind ILaunchesAPI::class
    singleOf(::LaunchesFilterDao) bind ILaunchesFilterDao::class


    singleOf(::LaunchesRepository) bind ILaunchesRepository::class
    singleOf(::LaunchesFilterRocketRepository) bind ILaunchesFilterRocketRepository::class
    singleOf(::LaunchesFilterRepository) bind ILaunchesFilterRepository::class
    factory { (filters: ILaunchesFilterRepository.Filters, pageSize: Int, forceRefresh: Boolean, ) ->
        LaunchesMediator(get(), get(), get(), get(), get(), filters, pageSize, forceRefresh)
    }


    viewModelOf(::LaunchSearchViewModel)
    viewModelOf(::LaunchFilterViewModel)
    viewModelOf(::LaunchFilterRocketViewModel)
}
