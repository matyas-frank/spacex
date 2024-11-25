package cz.frank.spacex.ships.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import cz.frank.spacex.ships.ui.search.ShipListDetail
import kotlinx.serialization.Serializable

@Serializable
object ShipsNavigation {
    @Serializable
    data object ListDetail

}

fun NavGraphBuilder.dragonsNavigation(toggleDrawer: () -> Unit) {
    navigation<ShipsNavigation>(ShipsNavigation.ListDetail) {
        composable<ShipsNavigation.ListDetail> {
            ShipListDetail(toggleDrawer)
        }
    }
}
