package cz.frank.spacex.starlink

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import cz.frank.spacex.main.ui.NavigationDrawerItem
import cz.frank.spacex.starlink.ui.map.StarlinkMap
import kotlinx.serialization.Serializable

object StarlinkNavigation {
    @Serializable
    data object Map
}

fun NavGraphBuilder.starlinkNavigation(navHostController: NavHostController, toggleDrawer: () -> Unit) {
    navigation<NavigationDrawerItem.Starlink>(StarlinkNavigation.Map) {
        composable<StarlinkNavigation.Map> {
            StarlinkMap(navHostController, toggleDrawer)
        }
    }
}
