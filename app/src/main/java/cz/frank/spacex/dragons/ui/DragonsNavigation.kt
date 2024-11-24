package cz.frank.spacex.dragons.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import cz.frank.spacex.dragons.ui.search.DragonListDetail
import kotlinx.serialization.Serializable

@Serializable
object DragonsNavigation {
    @Serializable
    data object ListDetail

}

fun NavGraphBuilder.dragonsNavigation(toggleDrawer: () -> Unit) {
    navigation<DragonsNavigation>(DragonsNavigation.ListDetail) {
        composable<DragonsNavigation.ListDetail> {
            DragonListDetail(toggleDrawer)
        }
    }
}
