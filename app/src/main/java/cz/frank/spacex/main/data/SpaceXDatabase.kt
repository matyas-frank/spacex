package cz.frank.spacex.main.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cz.frank.spacex.launches.data.database.dao.LaunchDao
import cz.frank.spacex.launches.data.database.entity.LaunchEntity

@Database(entities = [LaunchEntity::class], version = 1, exportSchema = true)
abstract class SpaceXDatabase : RoomDatabase() {
    abstract fun launchesDao(): LaunchDao

    companion object {
        fun create(applicationContext: Context) = Room.databaseBuilder(
            applicationContext,
            SpaceXDatabase::class.java, "spacex"
        ).build()
    }
}
