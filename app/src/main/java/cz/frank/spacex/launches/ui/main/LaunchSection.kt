package cz.frank.spacex.launches.ui.main

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavHostController
import cz.frank.spacex.launches.ui.LaunchesNavigation
import cz.frank.spacex.launches.ui.detail.LaunchDetail
import cz.frank.spacex.launches.ui.detail.LaunchDetailScreen
import cz.frank.spacex.launches.ui.search.LaunchesSearchScreen

@Composable fun LaunchesSection(
    navHostController: NavHostController,
    toggleDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(
        onFilterClick = { navHostController.navigate(LaunchesNavigation.Filter) },
        toggleDrawer,
        modifier,
    )
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
private fun Layout(
    onFilterClick: () -> Unit,
    toggleDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<LaunchDetail>()
    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }
    SharedTransitionLayout { ->
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    LaunchesSearchScreen(
                        navigateToFilter = onFilterClick,
                        navigateToDetail = { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it) },
                        toggleDrawer = toggleDrawer,
                        textAnimationModifier = {
                            Modifier.sharedElement(
                                rememberSharedContentState(key = "text-${it.id}"),
                                animatedVisibilityScope = this
                            )
                        },
                    )
                }

            },
            detailPane = {
                AnimatedPane {
                    navigator.currentDestination?.content?.let {
                        LaunchDetailScreen(
                            it,
                            { if (navigator.canNavigateBack()) navigator.navigateBack() },
                        )
                    }

                }
            },
            modifier = modifier
        )
    }
}
