package cz.frank.spacex.crew

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import cz.frank.spacex.main.ui.NavigationDrawerItem
import cz.frank.spacex.crew.ui.search.CrewSearchScreen
import kotlinx.serialization.Serializable

object CrewNavigation {
    @Serializable
    data object Search
}

fun NavGraphBuilder.crewNavigation(toggleDrawer: () -> Unit) {
    navigation<NavigationDrawerItem.Crew>(CrewNavigation.Search) {
        composable<CrewNavigation.Search> {
            CrewSearchScreen(toggleDrawer)
        }
    }
}
