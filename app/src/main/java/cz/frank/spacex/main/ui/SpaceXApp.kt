package cz.frank.spacex.main.ui

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cz.frank.spacex.launches.ui.launchesNavigation
import cz.frank.spacex.starlink.nextLaunchNavigation
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable fun SpaceXApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    fun toggleDrawer() {
        scope.launch {
            drawerState.apply {
                if (isClosed) open() else close()
            }
        }
    }
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                isDrawerItemSelected = { drawerItem ->
                    currentDestination?.hierarchy?.any { it.hasRoute(drawerItem::class) } == true
                },
                onItemClick = {
                    toggleDrawer()
                    navController.drawerItemNavigation(it)
                },
            )
        },
        modifier = modifier,
        drawerState = drawerState,
    ) {
        Navigation(navController, ::toggleDrawer)
    }
}

@Composable private fun Navigation(
    navController: NavHostController,
    toggleDrawer: () -> Unit
) {
    NavHost(navController, NavigationDrawerItem.Launches) {
        spaceXNavigationGraph(
            navController,
            toggleDrawer
        )
    }
}

private fun NavGraphBuilder.spaceXNavigationGraph(navHostController: NavHostController, toggleDrawer: () -> Unit) {
    launchesNavigation(navHostController, toggleDrawer)
    nextLaunchNavigation(navHostController, toggleDrawer)
}

private fun NavHostController.drawerItemNavigation(item: Any) {
    navigate(item) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

sealed interface NavigationDrawerItem {
    @Serializable data object Launches : NavigationDrawerItem
    @Serializable data object NextLaunch : NavigationDrawerItem
}
