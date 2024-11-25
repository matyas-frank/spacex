package cz.frank.spacex.ships.ui.search

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPaneScope
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.frank.spacex.R
import cz.frank.spacex.ships.ui.main.ShipDetail
import cz.frank.spacex.ships.ui.main.ShipUI

// Create some simple sample data
val data = listOf(
    ShipUI("0", "Android", "ASdasdasdasdas")
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable fun ShipsSearchScreen(
    navigator: ThreePaneScaffoldNavigator<ShipDetail>,
    animatedPaneScope: AnimatedPaneScope,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
    toggleDrawer: () -> Unit,
) {
    Layout(toggleDrawer, navigator, animatedPaneScope, sharedTransitionScope, modifier)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
private fun Layout(
    toggleDrawer: () -> Unit,
    navigator: ThreePaneScaffoldNavigator<ShipDetail>,
    animatedPaneScope: AnimatedPaneScope,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dragon_search)) },
                navigationIcon = {
                    IconButton(toggleDrawer) {
                        Icon(Icons.Default.Menu, null)
                    }
                },
            )
        }
    ) {
        LazyColumn(Modifier.padding(it)) {
            items(data.toList()) {
                Ship(navigator, it, animatedPaneScope, sharedTransitionScope)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable private fun Ship(
    navigator: ThreePaneScaffoldNavigator<ShipDetail>,
    dragon: ShipUI,
    animatedPaneScope: AnimatedPaneScope,
    sharedTransitionScope: SharedTransitionScope,
) {
    Column {
        ListItem(
            modifier = Modifier.clickable {
                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, ShipDetail(dragon.id))
            },
            headlineContent = {
                with(sharedTransitionScope) {
                    Text(
                        text = dragon.title,
                        modifier = Modifier
                            .padding(16.dp)
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = "text-$dragon"),
                                animatedVisibilityScope = animatedPaneScope
                            )
                    )
                }
            },
        )
        HorizontalDivider()
    }
}


