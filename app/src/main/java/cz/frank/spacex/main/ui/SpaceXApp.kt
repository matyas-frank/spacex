package cz.frank.spacex.main.ui

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import cz.frank.spacex.ships.ui.ShipsNavigation
import cz.frank.spacex.ships.ui.dragonsNavigation
import cz.frank.spacex.starlink.StarlinkNavigation
import cz.frank.spacex.starlink.starlinkNavigation
import kotlinx.coroutines.launch

@Composable fun SpaceXApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedDrawerSection by rememberSaveable { mutableStateOf(DrawerItem.Ships) }
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
                selectedDrawerSection,
                onItemClick = {
                    selectedDrawerSection = it
                    toggleDrawer()
                    when (it) {
                        DrawerItem.Ships -> {
                            navController.navigate(ShipsNavigation) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        DrawerItem.Starlink -> {
                            navController.navigate(StarlinkNavigation) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
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
    NavHost(navController, ShipsNavigation) {
        spaceXNavigationGraph(
            toggleDrawer = toggleDrawer
        )
    }
}

private fun NavGraphBuilder.spaceXNavigationGraph(toggleDrawer: () -> Unit) {
    dragonsNavigation(toggleDrawer)
    starlinkNavigation(toggleDrawer)
}
