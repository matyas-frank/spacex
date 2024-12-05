package cz.frank.spacex.launches.ui

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import cz.frank.spacex.launches.ui.detail.LaunchDetailScreen
import cz.frank.spacex.launches.ui.filter.LaunchesFilterScreen
import cz.frank.spacex.launches.ui.filter.rocket.LaunchFilterRocketScreen
import cz.frank.spacex.launches.ui.search.LaunchesSearchScreen
import cz.frank.spacex.main.ui.NavigationDrawerItem
import kotlinx.serialization.Serializable

fun NavGraphBuilder.launchesNavigation(navHostController: NavHostController, toggleDrawer: () -> Unit) {
    navigation<NavigationDrawerItem.Launches>(LaunchesNavigation.List) {
        composable<LaunchesNavigation.List> {
            LaunchesSearchScreen(
                navigateToFilter = { navHostController.navigate(LaunchesNavigation.Filter) },
                navigateToDetail = { navHostController.navigate(LaunchesNavigation.Detail(it.id)) },
                toggleDrawer,
            )
        }
        composable<LaunchesNavigation.Detail>(
            enterTransition = { slideInHorizontally{ it } },
            exitTransition = { slideOutHorizontally { it } }
        ) {
            val id = it.toRoute<LaunchesNavigation.Detail>().id
            LaunchDetailScreen(id, { navHostController.navigateUp() })
        }
        composable<LaunchesNavigation.Filter> {
            LaunchesFilterScreen(
                onBackClick = { navHostController.navigateUp() },
                navigateToRockets = { navHostController.navigate(LaunchesNavigation.Rockets) }
            )
        }
        composable<LaunchesNavigation.Rockets> {
            LaunchFilterRocketScreen(
                onBackClick = { navHostController.navigateUp() }
            )
        }
    }
}

object LaunchesNavigation {
    @Serializable
    data object List

    @Serializable
    data class Detail(val id: String)

    @Serializable
    data object Filter

    @Serializable
    data object Rockets
}
