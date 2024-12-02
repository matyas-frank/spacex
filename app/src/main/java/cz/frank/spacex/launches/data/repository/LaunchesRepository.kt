package cz.frank.spacex.launches.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import cz.frank.spacex.launches.data.database.dao.LaunchDao
import cz.frank.spacex.launches.ui.search.LaunchPreviewModel
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class LaunchesRepository(
    private val launchDao: LaunchDao,
 ) : KoinComponent {

    @OptIn(ExperimentalPagingApi::class)
    fun getPager(filters: ILaunchesFilterRepository.Filters) = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE),
        pagingSourceFactory = { launchDao.getAllLaunches() },
        remoteMediator = inject<LaunchesMediator> { parametersOf(filters, PAGE_SIZE) } .value
    ).flow.map { items ->
        items.map {
            LaunchPreviewModel(
                it.id,
                it.name,
                it.image,
                it.rocket,
                if (it.upcoming) LaunchPreviewModel.State.Upcoming
                else LaunchPreviewModel.State.Launched(it.success)
            )
        }
    }
}

private const val PAGE_SIZE = 50
