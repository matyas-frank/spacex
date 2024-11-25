package cz.frank.spacex.ships.ui.main

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.frank.spacex.ships.ui.detail.ShipDetailScreen
import cz.frank.spacex.ships.ui.search.ShipsSearchScreen
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShipUI(
    val id: String,
    val title: String,
    val image: String,
) : Parcelable

@Parcelize
data class ShipDetail(val id: String) : Parcelable

@Composable fun ShipsSection(modifier: Modifier = Modifier, toggleDrawer: () -> Unit) {
    Layout(modifier, toggleDrawer)
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
private fun Layout(modifier: Modifier = Modifier, toggleDrawer: () -> Unit) {
    val navigator = rememberListDetailPaneScaffoldNavigator<ShipDetail>()
    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }
    SharedTransitionLayout(modifier) {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    ShipsSearchScreen(
                        navigator,
                        this@AnimatedPane,
                        this@SharedTransitionLayout,
                        toggleDrawer = toggleDrawer
                    )
                }

            },
            detailPane = {
                AnimatedPane {
                    navigator.currentDestination?.content?.let {
                        ShipDetailScreen(
                            it,
                            navigator,
                            this@AnimatedPane,
                            this@SharedTransitionLayout
                        )
                    }

                }
            },
        )
    }
}
