package cz.frank.spacex.launches.data.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cz.frank.spacex.launches.data.database.entity.LaunchEntity

@Dao
interface LaunchDao {
    @Insert fun insertAllLaunches(vararg launches: LaunchEntity)

    @Query("DELETE FROM launch") fun deleteAllLaunches()

    @Query("Select * FROM launch") fun getAllLaunches(): PagingSource<Int, LaunchEntity>
}
