package cz.frank.spacex.sharedtests

import cz.frank.spacex.launches.data.database.dao.IRemoteKeyDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRemoteKeyDao: IRemoteKeyDao {
    private var _nextPageToLoad = MutableStateFlow<Int?>(null)
    override val nextPageToLoad: Flow<Int?> = _nextPageToLoad

    override suspend fun updateNextPage(page: Int) {
        _nextPageToLoad.value = page
    }
}
