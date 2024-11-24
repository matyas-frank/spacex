package cz.frank.spacex.starlink

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import cz.frank.spacex.starlink.ui.map.StarlinkMap
import kotlinx.serialization.Serializable

@Serializable
object StarlinkNavigation {
    @Serializable
    data object Map
}

fun NavGraphBuilder.starlinkNavigation(toggleDrawer: () -> Unit) {
    navigation<StarlinkNavigation>(StarlinkNavigation.Map) {
        composable<StarlinkNavigation.Map> {
            StarlinkMap(toggleDrawer)
        }
    }
}
