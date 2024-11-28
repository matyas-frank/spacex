package cz.frank.spacex.launches.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import cz.frank.spacex.launches.ui.filter.LaunchesFilterScreen
import cz.frank.spacex.launches.ui.main.LaunchesSection
import cz.frank.spacex.main.ui.NavigationDrawerItem
import kotlinx.serialization.Serializable

fun NavGraphBuilder.launchesNavigation(navHostController: NavHostController, toggleDrawer: () -> Unit) {
    navigation<NavigationDrawerItem.Launches>(LaunchesNavigation.ListDetail) {
        composable<LaunchesNavigation.ListDetail> {
            LaunchesSection(navHostController, toggleDrawer)
        }
        composable<LaunchesNavigation.Filter> {
            LaunchesFilterScreen()
        }
    }
}

object LaunchesNavigation {
    @Serializable
    data object ListDetail

    @Serializable
    data object Filter
}