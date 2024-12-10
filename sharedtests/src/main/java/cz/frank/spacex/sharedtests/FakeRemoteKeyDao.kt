package cz.frank.spacex.sharedtests

import cz.frank.spacex.launches.data.database.dao.IRemoteKeyDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeRemoteKeyDao: IRemoteKeyDao {
    override val defaultPageToLoad: Int = 1
    private var _nextPageToLoad = defaultPageToLoad
    override val nextPageToLoad: Flow<Int> = flow { _nextPageToLoad }

    override suspend fun updateNextPage(page: Int) {
        _nextPageToLoad = page
    }
}
