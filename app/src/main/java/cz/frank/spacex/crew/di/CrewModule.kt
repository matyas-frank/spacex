package cz.frank.spacex.crew.di

import cz.frank.spacex.crew.data.CrewApi
import cz.frank.spacex.crew.data.CrewRepository
import cz.frank.spacex.crew.data.ICrewApi
import cz.frank.spacex.crew.ui.search.CrewSearchViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val crewModule = module {
    singleOf(::CrewApi) bind ICrewApi::class
    singleOf(::CrewRepository)

    viewModelOf(::CrewSearchViewModel)
}
