package cz.frank.spacex.launches.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import cz.frank.spacex.R
import cz.frank.spacex.launches.ui.detail.LaunchDetail
import cz.frank.spacex.shared.ui.theme.SpaceXTheme
import org.koin.compose.viewmodel.koinViewModel

// Create some simple sample data
val data = listOf(
    LaunchPreviewModel(
        id = 1,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/40/e3/GypSkayF_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(true)
    ),
    LaunchPreviewModel(
        id = 2,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/75/39/TJU6xWM5_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(false)
    ),
    LaunchPreviewModel(
        id = 3,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/a6/9b/IzWT1pYC_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Upcoming
    ),
    LaunchPreviewModel(
        id = 4,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/40/e3/GypSkayF_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(true)
    ),
    LaunchPreviewModel(
        id = 5,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/75/39/TJU6xWM5_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(false)
    ),
    LaunchPreviewModel(
        id = 6,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/a6/9b/IzWT1pYC_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Upcoming
    ),
    LaunchPreviewModel(
        id = 7,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/40/e3/GypSkayF_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(true)
    ),
    LaunchPreviewModel(
        id = 8,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/75/39/TJU6xWM5_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(false)
    ),
    LaunchPreviewModel(
        id = 9,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/a6/9b/IzWT1pYC_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Upcoming
    ),
    LaunchPreviewModel(
        id = 10,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/40/e3/GypSkayF_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(true)
    ),
    LaunchPreviewModel(
        id = 11,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/75/39/TJU6xWM5_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Launched(false)
    ),
    LaunchPreviewModel(
        id = 12,
        title = "FalconSat",
        patch = "https://images2.imgbox.com/a6/9b/IzWT1pYC_o.png",
        rocket = "Falcon 1",
        state = LaunchPreviewModel.State.Upcoming
    ),
)

@Composable fun LaunchesSearchScreen(
    navigateToFilter: () -> Unit,
    navigateToDetail: (LaunchDetail) -> Unit,
    toggleDrawer: () -> Unit,
    textAnimationModifier: @Composable (LaunchDetail) -> Modifier,
    modifier: Modifier = Modifier,
    vm: LaunchSearchViewModel = koinViewModel(),
) {
    val query by vm.query.collectAsState()
    LaunchesScreenLayout(
        query,
        vm::onQueryChange,
        LaunchSearchViewModel::isQueryEmpty,
        toggleDrawer,
        navigateToDetail,
        vm::eraseQuery,
        navigateToFilter,
        modifier,
        textAnimationModifier,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LaunchesScreenLayout(
    query: String,
    onQueryChange: (String) -> Unit,
    isQueryEmpty: (String) -> Boolean,
    toggleDrawer: () -> Unit,
    navigateToDetail: (LaunchDetail) -> Unit,
    onEraseQueryClick: () -> Unit,
    onFilterScreenClick: () -> Unit,
    modifier: Modifier = Modifier,
    textAnimationModifier: @Composable (LaunchDetail) -> Modifier,
) {
    val scrollState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(scrollState)

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
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
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                        },
                    )
                }, navigationIcon = {
                    IconButton(toggleDrawer) {
                        Icon(Icons.Default.Menu, null)
                    }
                }, actions = {
                    IconButton(onFilterScreenClick) {
                        Icon(painterResource(R.drawable.ic_filter_list), null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) {
        Surface(Modifier.padding(it)) {
            LazyColumn(verticalArrangement = Arrangement.Top) {
                items(data.toList()) { launch ->
                    LaunchItem(launch, navigateToDetail, textAnimationModifier)
                }
            }
        }
    }
}

@Composable private fun LaunchItem(
    model: LaunchPreviewModel,
    navigateToDetail: (LaunchDetail) -> Unit,
    textAnimationModifier: @Composable (LaunchDetail) -> Modifier
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
    Card(shape = CircleShape) {
        Box(Modifier.padding(8.dp)){
            val iconSize = 28.dp
            when (state) {
                is LaunchPreviewModel.State.Upcoming -> {
                    val extraPadding = 2.dp
                    Icon(
                        painterResource(R.drawable.ic_event_upcoming),
                        null,
                        Modifier
                            .padding(extraPadding)
                            .size(iconSize - extraPadding)
                    )
                }
                is LaunchPreviewModel.State.Launched ->
                    Icon(if (state.wasSuccessful) Icons.Default.Check else Icons.Default.Close,
                        null,
                        Modifier.size(iconSize)
                    )
            }
        }
    }
}

@Composable private fun LaunchItemImage(url: String) {
    val imagePainter = rememberAsyncImagePainter(
        url,
        onState = {  }
    )
    Card(
        Modifier.size(60.dp),
        shape = CircleShape,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Image(
            painter = imagePainter,
            contentDescription = "",
            Modifier.padding(6.dp)
        )
    }
}


@Preview
@Composable
private fun Preview() {
    LaunchesScreenLayout(
        "Query",
        { _ -> },
        { _ -> false },
        {},
        { },
        onEraseQueryClick = {},
        onFilterScreenClick = {},
        Modifier,
        { _ -> Modifier },
    )
}

@Preview
@Composable private fun LaunchItemPreview() {
    SpaceXTheme {
        Box(Modifier.padding(top = 40.dp)) {
            Column {
                LaunchItem(
                    LaunchPreviewModel(
                        id = 1,
                        title = "FalconSat",
                        patch = "https://images2.imgbox.com/40/e3/GypSkayF_o.png",
                        rocket = "Falcon 1",
                        state = LaunchPreviewModel.State.Launched(true)
                    ),
                    navigateToDetail = { },
                    { Modifier },
                )
                HorizontalDivider()
                LaunchItem(
                    LaunchPreviewModel(
                        id = 1,
                        title = "FalconSat",
                        patch = "https://images2.imgbox.com/75/39/TJU6xWM5_o.png",
                        rocket = "Falcon 1",
                        state = LaunchPreviewModel.State.Launched(false)
                    ),
                    navigateToDetail = { },
                    { Modifier },
                )
                HorizontalDivider()
                LaunchItem(
                    LaunchPreviewModel(
                        id = 1,
                        title = "FalconSat",
                        patch = "https://images2.imgbox.com/a6/9b/IzWT1pYC_o.png",
                        rocket = "Falcon 1",
                        state = LaunchPreviewModel.State.Upcoming
                    ),
                    navigateToDetail = { },
                    { Modifier },
                )
            }
        }
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
