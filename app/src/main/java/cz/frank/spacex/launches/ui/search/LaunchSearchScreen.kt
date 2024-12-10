package cz.frank.spacex.launches.ui.search

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import cz.frank.spacex.R
import cz.frank.spacex.launches.domain.model.LaunchPreviewModel
import cz.frank.spacex.launches.ui.detail.LaunchDetail
import cz.frank.spacex.main.ui.theme.SpaceXTheme
import cz.frank.spacex.main.ui.theme.attentionColor
import cz.frank.spacex.main.ui.theme.failureColor
import cz.frank.spacex.main.ui.theme.successColor
import cz.frank.spacex.shared.ui.CachedRemoteImage
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable fun LaunchesSearchScreen(
    navigateToFilter: () -> Unit,
    navigateToDetail: (LaunchDetail) -> Unit,
    toggleDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    vm: LaunchSearchViewModel = koinViewModel(),
) {
    val query by vm.query.collectAsStateWithLifecycle()
    val isAnyFilterActive by vm.isAnyFilterActive.collectAsStateWithLifecycle()
    val items = vm.pager.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    LaunchesScreenLayout(
        items,
        listState,
        query,
        isAnyFilterActive,
        vm::onQueryChange,
        LaunchSearchViewModel::isQueryEmpty,
        toggleDrawer,
        navigateToDetail,
        vm::eraseQuery,
        navigateToFilter,
        modifier,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class) private fun LaunchesScreenLayout(
    items: LazyPagingItems<LaunchPreviewModel>,
    listState: LazyListState,
    query: String,
    isAnyFilterActive: Boolean,
    onQueryChange: (String) -> Unit,
    isQueryEmpty: (String) -> Boolean,
    toggleDrawer: () -> Unit,
    navigateToDetail: (LaunchDetail) -> Unit,
    onEraseQueryClick: () -> Unit,
    onFilterScreenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(scrollState)

    LaunchedEffect(items) {
        var loadingTriggered = false
        snapshotFlow { items.loadState.refresh }.collect {
            when (it) {
                is LoadState.Loading -> {
                    loadingTriggered = true
                }
                is LoadState.NotLoading -> {
                    if (loadingTriggered) {
                        listState.animateScrollToItem(0)
                        loadingTriggered = false
                    }
                }
                is LoadState.Error -> {}
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                query,
                onQueryChange,
                isQueryEmpty,
                onEraseQueryClick,
                toggleDrawer,
                onFilterScreenClick,
                isAnyFilterActive,
                scrollBehavior
            )
        },
    ) { padding ->
        SearchContent(padding, items, listState, navigateToDetail)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun TopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isQueryEmpty: (String) -> Boolean,
    onEraseQueryClick: () -> Unit,
    toggleDrawer: () -> Unit,
    onFilterScreenClick: () -> Unit,
    isAnyFilterActive: Boolean,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { /* Not necessary because search is immediate after typing */ },
                expanded = true,
                onExpandedChange = { /* Search view is permanent */ },
                placeholder = { Text(stringResource(R.string.launch_search_input_field_placeholder)) },
                trailingIcon = {
                    AnimatedVisibility(!isQueryEmpty(query), enter = fadeIn(), exit = fadeOut()) {
                        IconButton(onClick = onEraseQueryClick) {
                            Icon(Icons.Default.Close, stringResource(R.string.erase_search_query_description))
                        }
                    }
                },
            )
        }, navigationIcon = {
            IconButton(toggleDrawer, Modifier.testTag("ToggleDrawerLaunches")) {
                Icon(Icons.Default.Menu, stringResource(R.string.toggle_drawer_icon_description))
            }
        }, actions = {
            IconButton(onFilterScreenClick) {
                BadgedBox(
                    badge = {
                        if (isAnyFilterActive) {
                            Badge()
                        }
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.ic_filter_list),
                        stringResource(R.string.navigate_to_filtering_description),
                        Modifier.padding(2.dp).testTag("Navigate to filter")
                    )
                }

            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SearchContent(
    paddingValues: PaddingValues,
    items: LazyPagingItems<LaunchPreviewModel>?,
    listState: LazyListState,
    navigateToDetail: (LaunchDetail) -> Unit
) {
    Surface(Modifier.padding(paddingValues)) {
        items?.let {
            var isIndicatorVisible by remember { mutableStateOf(false) }
            LaunchedEffect(items.loadState.refresh) {
                items.let {
                    if (items.loadState.refresh is LoadState.NotLoading) isIndicatorVisible = false
                }
            }

            val state = rememberPullToRefreshState()
            PullToRefreshBox(
                items.loadState.refresh is LoadState.Loading && isIndicatorVisible,
                onRefresh = {
                    isIndicatorVisible = true
                    items.refresh()
                },
                Modifier.fillMaxWidth(),
                state,
                indicator = {
                    Indicator(
                        state = state,
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = items.loadState.refresh is LoadState.Loading,

                        )
                },
            ) {
                if (items.itemCount != 0) {
                    Launches(items, listState, navigateToDetail)
                } else {
                    if (items.loadState.isIdle) {
                        if (items.loadState.hasError) {
                            FailureResult { items.refresh() }
                        } else {
                            EmptyResult()
                        }
                    } else {
                        if (items.loadState.hasError) {
                            FailureResult { items.refresh() }
                        } else {
                            RefreshLoadingIndicator()
                        }

                    }
                }
            }
        }
    }
}

@Composable private fun EmptyResult() {
    ResultCard(R.string.launches_search_empty_results)
}

@Composable private fun FailureResult(onRefreshClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ResultCard(R.string.launches_search_failure_result)
        RefreshButton(onRefreshClick)
    }
}

@Composable private fun ResultCard(textRes: Int) {
    Card(Modifier.padding(32.dp)) {
        Column(
            Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(textRes))
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun Launches(
    items: LazyPagingItems<LaunchPreviewModel>,
    listState: LazyListState,
    navigateToDetail: (LaunchDetail) -> Unit
) {
    LazyColumn(
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        stickyHeader {
            AnimatedVisibility(
                items.loadState.refresh == LoadState.Loading,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) { RefreshLoadingIndicator() }
        }
        item {
            AnimatedVisibility(
                items.loadState.refresh is LoadState.Error,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) { RefreshButton { items.refresh() } }
        }
        items(
            items.itemCount,
            key = items.itemKey { it.id }
        ) { index ->
            items[index]?.let {
                LaunchItem(it, navigateToDetail)
            } ?: PlaceHolder()
        }

        item {
            when {
                items.loadState.refresh is LoadState.Error -> {
                    RefreshButton { items.refresh() }
                }

                items.loadState.append is LoadState.Error -> {
                    RetryButton { items.retry() }
                }

                items.loadState.append == LoadState.Loading -> {
                    AppendLoadingIndicator()
                }
            }
        }
    }
}

@Composable private fun RetryButton(onRefreshClick: () -> Unit) {
    Button(onClick = onRefreshClick, Modifier.padding(16.dp)) {
        Text(stringResource(R.string.launch_search_retry_button))
    }
}

@Composable private fun RefreshButton(onRefreshClick: () -> Unit) {
    Button(onClick = onRefreshClick, Modifier.padding(16.dp)) {
        Text(stringResource(R.string.launch_search_refresh_button))
    }
}

@Composable private fun RefreshLoadingIndicator() {
    LinearProgressIndicator(Modifier.fillMaxWidth())
}

@Composable private fun AppendLoadingIndicator() {
    CircularProgressIndicator(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
    )
}

@Composable private fun PlaceHolder() {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    Surface(
        Modifier
            .height(80.dp)
            .fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(Modifier.size(50.dp), shape = RectangleShape, color = color) {}
            Surface(
                Modifier
                    .padding(16.dp)
                    .height(20.dp)
                    .fillMaxWidth(), shape = RectangleShape, color = color) {}

        }
    }
}

@Composable private fun LaunchItem(
    model: LaunchPreviewModel,
    navigateToDetail: (LaunchDetail) -> Unit,
) {
    Column {
        ListItem(
            modifier = Modifier
                .clickable { navigateToDetail(LaunchDetail(model.id)) }
                .fillMaxWidth(),
            headlineContent = { Text(model.title) },
            leadingContent = { LaunchItemImage(model.patch) },
            trailingContent = { LaunchItemIcon(model.state) },
            supportingContent = { Text(model.rocket) }
        )
        HorizontalDivider()
    }
}

@Composable private fun LaunchItemIcon(state: LaunchPreviewModel.State) {
    val color = when (state) {
        LaunchPreviewModel.State.Upcoming -> attentionColor
        is LaunchPreviewModel.State.Launched -> when (state.wasSuccessful) {
            true -> successColor
            false -> failureColor
            null -> attentionColor
        }
    }
    Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = color)) {
        Box(Modifier.padding(8.dp)){
            val iconSize = 28.dp
            when (state) {
                is LaunchPreviewModel.State.Upcoming -> {
                    val extraPadding = 2.dp
                    Icon(
                        painterResource(R.drawable.ic_event_upcoming),
                        stringResource(R.string.launch_state_upcoming_launch_description),
                        Modifier
                            .padding(extraPadding)
                            .size(iconSize - extraPadding)
                    )
                }
                is LaunchPreviewModel.State.Launched ->
                    state.wasSuccessful?.let {
                        Icon(
                            if (state.wasSuccessful) Icons.Default.Check else Icons.Default.Close,
                            stringResource(
                                if (state.wasSuccessful) R.string.launch_state_successfully_launched_description
                                else R.string.launch_state_unsuccessful_launch_description
                            ),
                            Modifier.size(iconSize)
                        )
                    } ?: Icon(
                        painterResource(R.drawable.ic_question_mark),
                        stringResource(R.string.launch_state_unknown_state_launch_description),
                        Modifier.size(iconSize)
                    )
            }
        }
    }
}

@Composable private fun LaunchItemImage(url: String?) {
    Card(
        Modifier.size(60.dp),
        shape = CircleShape,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        CachedRemoteImage(
            url,
            contentDescription = "",
            Modifier.padding(6.dp)
        )
    }
}

val data = listOf(
    LaunchPreviewModel(
        id = "1",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/40/e3/GypSkayF_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(true)
    ),
    LaunchPreviewModel(
        id = "2",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/75/39/TJU6xWM5_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(false)
    ),
    LaunchPreviewModel(
        id = "3",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/a6/9b/IzWT1pYC_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Upcoming
    ),
    LaunchPreviewModel(
        id = "4",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/40/e3/GypSkayF_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(true)
    ),
    LaunchPreviewModel(
        id = "5",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/75/39/TJU6xWM5_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(false)
    ),
    LaunchPreviewModel(
        id = "6",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/a6/9b/IzWT1pYC_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Upcoming
    ),
    LaunchPreviewModel(
        id = "7",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/40/e3/GypSkayF_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(true)
    ),
    LaunchPreviewModel(
        id = "8",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/75/39/TJU6xWM5_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(false)
    ),
    LaunchPreviewModel(
        id = "9",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/a6/9b/IzWT1pYC_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Upcoming
    ),
    LaunchPreviewModel(
        id = "10",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/40/e3/GypSkayF_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(true)
    ),
    LaunchPreviewModel(
        id = "11",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/75/39/TJU6xWM5_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(false)
    ),
    LaunchPreviewModel(
        id = "12",
        title = "FalconSat",
        patch = "https://images2.imgbox.com/a6/9b/IzWT1pYC_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Upcoming
    ),
)

@Preview
@Composable
private fun ScreenPreview() {
    val flow = remember {
        val fakeData = data // create pagingData from a list of fake data
        val pagingData = PagingData.from(fakeData)
        MutableStateFlow(pagingData)
    }
    val items = flow.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    LaunchesScreenLayout(
        items,
        listState,
        "Query",
        true,
        { _ -> },
        { _ -> false },
        {},
        { },
        onEraseQueryClick = {},
        onFilterScreenClick = {},
        Modifier,
    )
}

@Preview
@Composable private fun LaunchItemPreview() {
    SpaceXTheme {
        Box(Modifier.padding(top = 40.dp)) {
            Column {
                LaunchItem(
                    LaunchPreviewModel(
                        id = "1",
                        title = "FalconSat",
                        patch = "https://images2.imgbox.com/40/e3/GypSkayF_o.png",
                        rocket = "Falcon 1",
                        state = LaunchPreviewModel.State.Launched(true)
                    ),
                    navigateToDetail = { },
                )
                HorizontalDivider()
                LaunchItem(
                    LaunchPreviewModel(
                        id = "1",
                        title = "FalconSat",
                        patch = "https://images2.imgbox.com/75/39/TJU6xWM5_o.png",
                        rocket = "Falcon 1",
                        state = LaunchPreviewModel.State.Launched(false)
                    ),
                    navigateToDetail = { },
                )
                HorizontalDivider()
                LaunchItem(
                    LaunchPreviewModel(
                        id = "1",
                        title = "FalconSat",
                        patch = "https://images2.imgbox.com/a6/9b/IzWT1pYC_o.png",
                        rocket = "Falcon 1",
                        state = LaunchPreviewModel.State.Upcoming
                    ),
                    navigateToDetail = { },
                )
            }
        }
    }
}


@Preview
@Composable
private fun PlaceHolderPreview() {
    SpaceXTheme {
        PlaceHolder()

    }
}


val a = """
    {
        "flight_number": 1,
        "mission_name": "FalconSat",
        "mission_id": [],
        "upcoming": false,
        "launch_year": "2006",
        "launch_date_unix": 1143239400,
        "launch_date_utc": "2006-03-24T22:30:00.000Z",
        "launch_date_local": "2006-03-25T10:30:00+12:00",
        "is_tentative": false,
        "tentative_max_precision": "hour",
        "tbd": false,
        "launch_window": 0,
        "rocket": {
            "rocket_id": "falcon1",
            "rocket_name": "Falcon 1",
            "rocket_type": "Merlin A",
            "first_stage": {
                "cores": [
                    {
                        "core_serial": "Merlin1A",
                        "flight": 1,
                        "block": null,
                        "gridfins": false,
                        "legs": false,
                        "reused": false,
                        "land_success": null,
                        "landing_intent": false,
                        "landing_type": null,
                        "landing_vehicle": null
                    }
                ]
            },
            "second_stage": {
                "block": 1,
                "payloads": [
                    {
                        "payload_id": "FalconSAT-2",
                        "norad_id": [],
                        "reused": false,
                        "customers": [
                            "DARPA"
                        ],
                        "nationality": "United States",
                        "manufacturer": "SSTL",
                        "payload_type": "Satellite",
                        "payload_mass_kg": 20,
                        "payload_mass_lbs": 43,
                        "orbit": "LEO",
                        "orbit_params": {
                            "reference_system": "geocentric",
                            "regime": "low-earth",
                            "longitude": null,
                            "semi_major_axis_km": null,
                            "eccentricity": null,
                            "periapsis_km": 400,
                            "apoapsis_km": 500,
                            "inclination_deg": 39,
                            "period_min": null,
                            "lifespan_years": null,
                            "epoch": null,
                            "mean_motion": null,
                            "raan": null,
                            "arg_of_pericenter": null,
                            "mean_anomaly": null
                        }
                    }
                ]
            },
            "fairings": {
                "reused": false,
                "recovery_attempt": false,
                "recovered": false,
                "ship": null
            }
        },
        "ships": [],
        "telemetry": {
            "flight_club": null
        },
        "launch_site": {
            "site_id": "kwajalein_atoll",
            "site_name": "Kwajalein Atoll",
            "site_name_long": "Kwajalein Atoll Omelek Island"
        },
        "launch_success": false,
        "launch_failure_details": {
            "time": 33,
            "altitude": null,
            "reason": "merlin engine failure"
        },
        "links": {
            "mission_patch": "https://images2.imgbox.com/40/e3/GypSkayF_o.png",
            "mission_patch_small": "https://images2.imgbox.com/3c/0e/T8iJcSN3_o.png",
            "reddit_campaign": null,
            "reddit_launch": null,
            "reddit_recovery": null,
            "reddit_media": null,
            "presskit": null,
            "article_link": "https://www.space.com/2196-spacex-inaugural-falcon-1-rocket-lost-launch.html",
            "wikipedia": "https://en.wikipedia.org/wiki/DemoSat",
            "video_link": "https://www.youtube.com/watch?v=0a_00nJ_Y88",
            "youtube_id": "0a_00nJ_Y88",
            "flickr_images": []
        },
        "details": "Engine failure at 33 seconds and loss of vehicle",
        "static_fire_date_utc": "2006-03-17T00:00:00.000Z",
        "static_fire_date_unix": 1142553600,
        "timeline": {
            "webcast_liftoff": 54
        },
        "crew": null
    }
""".trimIndent()
