package cz.frank.spacex.launches.ui.detail

import android.os.Parcelable
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPaneScope
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.frank.spacex.launches.ui.search.data
import kotlinx.parcelize.Parcelize

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable fun LaunchDetailScreen(
    selectedTopic: LaunchDetail,
    navigator: ThreePaneScaffoldNavigator<LaunchDetail>,
    animatedPaneScope: AnimatedPaneScope,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
) {
    with(sharedTransitionScope) {
        Scaffold(
            modifier,
            topBar = {
            TopAppBar(
                title = { Text("Launch detail") },
                navigationIcon = {
                    IconButton({
                        if (navigator.canNavigateBack()) {
                            navigator.navigateBack()
                        }
                    }) { Icon(Icons.AutoMirrored.Default.ArrowBack, null) }
                }
            )
        }) {
            Column(Modifier.padding(it)) {
                Text(
                    selectedTopic.id.toString(),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(key = "text-${selectedTopic.id}"),
                            animatedVisibilityScope = animatedPaneScope
                        ),
                    style = MaterialTheme.typography.titleLarge,

                    )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(all = 4.dp)
                        .clickable {

                        }
                ) {
                    Text(
                        text = data.get(selectedTopic.id.toInt()).title,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Parcelize
data class LaunchDetail(val id: String) : Parcelable
