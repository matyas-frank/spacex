package cz.frank.spacex.launches.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launch")
data class LaunchEntity(
    @PrimaryKey val id: String,
    val name: String,
    val image: String?,
    val upcoming: Boolean,
    val success: Boolean?,
    val rocket: String,
)
