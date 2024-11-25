package cz.frank.spacex.ships.ui.search

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.AnimatedPaneScope
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.frank.spacex.R
import cz.frank.spacex.ships.ui.detail.ShipDetailScreen
import kotlinx.parcelize.Parcelize

// Create some simple sample data
val data = listOf(
    ShipUI("0", "Android", "ASdasdasdasdas")
)

@Parcelize
data class ShipUI(
    val id: String,
    val title: String,
    val image: String,
) : Parcelable

@Parcelize
data class ShipDetail(val id: String) : Parcelable

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable fun ShipListDetail(toggleDrawer: () -> Unit) {
    val navigator = rememberListDetailPaneScaffoldNavigator<ShipDetail>()
    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }
    SharedTransitionLayout {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    Ships(navigator, this@AnimatedPane, this@SharedTransitionLayout, toggleDrawer)
                }

            },
            detailPane = {
                AnimatedPane {
                    navigator.currentDestination?.content?.let {
                        ShipDetailScreen(it, navigator, this@AnimatedPane, this@SharedTransitionLayout)
                    }

                }
            },
        )
    }

}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable private fun Ships(
    navigator: ThreePaneScaffoldNavigator<ShipDetail>,
    animatedPaneScope: AnimatedPaneScope,
    sharedTransitionScope: SharedTransitionScope,
    toggleDrawer: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dragon_search)) },
                navigationIcon = { IconButton(toggleDrawer) {
                    Icon(Icons.Default.Menu, null)
                } },
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


