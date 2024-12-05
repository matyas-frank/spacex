package cz.frank.spacex.launches.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import cz.frank.spacex.launches.ui.detail.LaunchDetail
import cz.frank.spacex.launches.ui.detail.LaunchDetailScreen
import cz.frank.spacex.launches.ui.filter.LaunchesFilterScreen
import cz.frank.spacex.launches.ui.filter.rocket.LaunchFilterRocketScreen
import cz.frank.spacex.launches.ui.main.LaunchesSection
import cz.frank.spacex.launches.ui.next.NextLaunchScreen
import cz.frank.spacex.main.ui.NavigationDrawerItem
import kotlinx.serialization.Serializable

fun NavGraphBuilder.launchesNavigation(navHostController: NavHostController, toggleDrawer: () -> Unit) {
    navigation<NavigationDrawerItem.Launches>(LaunchesNavigation.ListDetail) {
        composable<LaunchesNavigation.ListDetail> {
            LaunchesSection(navHostController, toggleDrawer)
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

    navigation<NavigationDrawerItem.NextLaunch>(NextLaunchNavigation.Main) {
        composable<NextLaunchNavigation.Main> {
            NextLaunchScreen({ id -> navHostController.navigate(NextLaunchNavigation.Detail(id)) }, toggleDrawer)
        }
        composable<NextLaunchNavigation.Detail> {
            val id = it.toRoute<NextLaunchNavigation.Detail>().id
            LaunchDetailScreen(LaunchDetail(id), { navHostController.navigateUp() })
        }
    }
}

object LaunchesNavigation {
    @Serializable
    data object ListDetail

    @Serializable
    data object Filter

    @Serializable
    data object Rockets
}

object NextLaunchNavigation {
    @Serializable
    data object Main

    @Serializable
    data class Detail(val id: String)
}
