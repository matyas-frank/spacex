package cz.frank.spacex.sharedtests

import cz.frank.spacex.launches.data.database.dao.IRefreshDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeRefreshDao: IRefreshDao {
    private var _timeOfLastRefreshInMillis: Long? = null

    override val timeOfLastRefreshInMillis: Flow<Long?> = flow { _timeOfLastRefreshInMillis }

    override suspend fun changeTimeOfLastUpdate(millis: Long?) {
        _timeOfLastRefreshInMillis = millis
    }
}
