package cz.frank.spacex.launches.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import cz.frank.spacex.launches.data.database.entity.LaunchEntity

@Dao
interface LaunchDao {
    @Insert
    fun insertAll(vararg launches: LaunchEntity)
}
