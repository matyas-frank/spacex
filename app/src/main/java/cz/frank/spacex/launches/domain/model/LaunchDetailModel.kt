package cz.frank.spacex.launches.domain.model

data class LaunchDetailModel(
    val id: String,
    val flightNumber: Int,
    val name: String,
    val state: State,
    val fairingsRecovered: Boolean?,
    val launchpad: Launchpad?,
    val rocket: Rocket,
    val detail: String?,
    val date: Long,
    val youtubeId: String?,
    val article: String?
) {
    data class Rocket(val name: String)
    data class Launchpad(val name: String)
    sealed interface State {
        data object Upcoming : State
        data class Launched(val wasSuccessful: Boolean?) : State
    }
}
