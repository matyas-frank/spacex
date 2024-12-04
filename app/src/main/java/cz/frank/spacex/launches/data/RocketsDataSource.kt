package cz.frank.spacex.launches.data

import kotlinx.serialization.Serializable

object RocketsDataSource {
    val rockets = listOf(
        RocketResponse("Falcon 1", "5e9d0d95eda69955f709d1eb"),
        RocketResponse("Falcon 9", "5e9d0d95eda69973a809d1ec"),
        RocketResponse("Falcon Heavy", "5e9d0d95eda69974db09d1ed"),
        RocketResponse("Starship", "5e9d0d96eda699382d09d1ee")
    )
}

@Serializable data class RocketResponse(val name: String, val id: String)
