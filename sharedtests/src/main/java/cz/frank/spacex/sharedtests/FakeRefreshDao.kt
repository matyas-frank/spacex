package cz.frank.spacex.sharedtests

import cz.frank.spacex.launches.data.database.dao.IRefreshDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRefreshDao: IRefreshDao {
    private var _timeOfLastRefreshInMillis = MutableStateFlow<Long?>(null)
    override val timeOfLastRefreshInMillis: Flow<Long?> = _timeOfLastRefreshInMillis

    override suspend fun changeTimeOfLastUpdate(millis: Long?) {
        _timeOfLastRefreshInMillis.value = millis
    }
}
