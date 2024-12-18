package cz.frank.spacex.crew.ui.search

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import cz.frank.spacex.R
import cz.frank.spacex.crew.domain.model.CrewMemberModel
import cz.frank.spacex.main.ui.theme.SpaceXTheme
import cz.frank.spacex.main.ui.theme.attentionColor
import cz.frank.spacex.main.ui.theme.failureColor
import cz.frank.spacex.main.ui.theme.successColor
import cz.frank.spacex.shared.ui.RefreshableCachedImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

@Composable fun CrewSearchScreen(
    toggleDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    vm: CrewSearchViewModel = koinViewModel()
) {
    val membersResult by vm.members.collectAsStateWithLifecycle()
    val isPullRefreshing by vm.isPullRefreshing.collectAsStateWithLifecycle()

    val context = LocalContext.current
    fun onLinkClick(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }

    CrewSearchLayout(
        membersResult,
        isPullRefreshing,
        vm::pullRefresh,
        vm::fetchCrew,
        ::onLinkClick,
        toggleDrawer,
        modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun CrewSearchLayout(
    membersResult: Result<ImmutableList<CrewMemberModel>>?,
    isPullRefreshing: Boolean,
    onPullRefresh: () -> Unit,
    retry: () -> Unit,
    onLinkClick: (String) -> Unit,
    toggleDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(scrollState)
    Scaffold(
        modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.crew_search_title)) },
                navigationIcon = {
                    IconButton(toggleDrawer, Modifier.testTag("ToggleDrawerCrew")) {
                        Icon(
                            Icons.Default.Menu,
                            stringResource(R.string.toggle_drawer_icon_description)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        Box(Modifier.padding(it)) {
            membersResult?.let { model ->
                model.fold(
                    onSuccess= { CrewMembers(it, isPullRefreshing, onPullRefresh, onLinkClick) },
                    onFailure = { FailureScreen(retry = retry) }
                )
            } ?: LoadingScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun CrewMembers(
    members: ImmutableList<CrewMemberModel>,
    isPullRefreshing: Boolean,
    onPullRefresh: () -> Unit,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(isPullRefreshing, onPullRefresh) {
        if (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
            LazyColumn(modifier) {
                items(members) {
                    CrewMember(it, onLinkClick)
                }
            }
        } else {
            LazyVerticalGrid(GridCells.Fixed(2), modifier, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                items(members) {
                    CrewMember(it, onLinkClick)
                }
            }
        }
    }
}

@Composable private fun CrewMember(member: CrewMemberModel, onLinkClick: (String) -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Card(
            onClick = { onLinkClick(member.link) },
            Modifier.padding(vertical = 16.dp)
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(member.name, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(16.dp))
                    ActiveIndicator(member.status)
                }
                Spacer(Modifier.height(8.dp))
                Card(shape = RoundedCornerShape(22.dp)) {
                    RefreshableCachedImage(
                        member.image,
                        contentDescription = null,
                        Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth

                    )
                }
            }
        }
    }
}

val CrewMemberModel.Status.color get() = when (this) {
    CrewMemberModel.Status.ACTIVE -> successColor
    CrewMemberModel.Status.INACTIVE -> attentionColor
    CrewMemberModel.Status.RETIRED -> failureColor
    CrewMemberModel.Status.UNKNOWN -> Color.Gray
}

val CrewMemberModel.Status.text get() = when (this) {
    CrewMemberModel.Status.ACTIVE -> R.string.crew_search_member_active
    CrewMemberModel.Status.INACTIVE -> R.string.crew_search_member_not_active
    CrewMemberModel.Status.RETIRED -> R.string.crew_search_member_retired
    CrewMemberModel.Status.UNKNOWN -> R.string.crew_search_member_unknown
}

@Composable private fun ActiveIndicator(state: CrewMemberModel.Status) {
    Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = state.color)) {
        Box(Modifier.padding(8.dp)) {
            Text(
                stringResource(state.text).toUpperCase(Locale.current),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable private fun FailureScreen(modifier: Modifier = Modifier, retry: () -> Unit) {
    Scaffold(modifier) {
        Box(
            Modifier
                .padding(it)
                .fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(retry) { Text(stringResource(R.string.launch_search_retry_button)) }
        }
    }
}

@Composable private fun LoadingScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier) {
        Box(
            Modifier
                .padding(it)
                .fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
private fun Prev() {
    SpaceXTheme {
        CrewSearchLayout(Result.success(persistentListOf(
            CrewMemberModel(
                "Robert Behnken",
                CrewMemberModel.Status.ACTIVE,
                "https://imgur.com/0smMgMH.png",
                "https://en.wikipedia.org/wiki/Robert_L._Behnken"
            ),
            CrewMemberModel(
                "Douglas Hurley",
                CrewMemberModel.Status.INACTIVE,
                "https://i.imgur.com/ooaayWf.png",
                "https://en.wikipedia.org/wiki/Douglas_G._Hurley"
            ),
            CrewMemberModel(
                "Shannon Walker",
                CrewMemberModel.Status.RETIRED,
                "https://i.imgur.com/ooaayWf.png",
                "\"https://en.wikipedia.org/wiki/Shannon_Walker"
            ),
            CrewMemberModel(
                "Soichi Noguchi",
                CrewMemberModel.Status.UNKNOWN,
                "https://imgur.com/7B1jxl8.png",
                "https://en.wikipedia.org/wiki/Soichi_Noguchi"
            )

        )
        ), false, {}, {}, {}, {})
    }
}
