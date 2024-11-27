package cz.frank.spacex.launches.ui.search

data class LaunchPreviewModel(
    val id: Int,
    val title: String,
    val patch: String,
    val rocket: String,
    val state: State,
) {
    sealed interface State {
        data object Upcoming : State
        data class Launched(val wasSuccessful: Boolean) : State
    }
}
