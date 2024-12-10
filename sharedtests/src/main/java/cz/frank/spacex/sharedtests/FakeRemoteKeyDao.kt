package cz.frank.spacex.sharedtests

import cz.frank.spacex.launches.data.database.dao.IRemoteKeyDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class FakeRemoteKeyDao: IRemoteKeyDao {
    override val defaultPageToLoad: Int = 1
    private var _nextPageToLoad = MutableStateFlow(defaultPageToLoad)
    override val nextPageToLoad: Flow<Int> = _nextPageToLoad

    override suspend fun updateNextPage(page: Int) {
        _nextPageToLoad.value = page
    }
}
