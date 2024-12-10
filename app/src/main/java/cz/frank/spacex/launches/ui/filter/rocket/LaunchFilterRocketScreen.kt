package cz.frank.spacex.launches.ui.filter.rocket

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.frank.spacex.R
import cz.frank.spacex.launches.data.repository.RocketFilterModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import org.koin.androidx.compose.koinViewModel

@Composable fun LaunchFilterRocketScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    vm: LaunchFilterRocketViewModel = koinViewModel()
) {
    val rockets by vm.rockets.collectAsState()
    LaunchFilterRocketsLayout(
        rockets,
        vm::checkedAllClick,
        vm::onCheckedChange,
        onBackClick ={
            vm.saveRockets()
            onBackClick()
        },
        modifier
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LaunchFilterRocketsLayout(
    rockets: ImmutableList<RocketFilterModel>,
    onCheckedAll: () -> Unit,
    onCheckedChange: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.launches_filter_rockets)) },
                navigationIcon = {
                    IconButton(onBackClick, Modifier.testTag("NavigateBack")) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            stringResource(R.string.navigate_back_description)
                        )
                    }
                },
                actions = {
                    Checkbox(
                        rockets.all { it.isSelected },
                        { onCheckedAll() },
                        Modifier.testTag("AllCheckbox")
                    )
                }
            )
        },
        modifier = modifier
    ) {
        LazyColumn(Modifier.padding(it)) {
            items(rockets) { Rocket(it, onCheckedChange) }
        }
    }
}

@Composable private fun Rocket(rocket: RocketFilterModel, onCheckedChange: (String) -> Unit) {
    Row(
        Modifier
            .clickable { onCheckedChange(rocket.id) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rocket.name)
        Spacer(Modifier.weight(1f))
        Checkbox(rocket.isSelected, { onCheckedChange(rocket.id) }, Modifier.testTag("RocketCheckbox"))
    }
    HorizontalDivider()
}

@Preview
@Composable
private fun Preview() {
    LaunchFilterRocketsLayout(
        rockets = listOf(
            RocketFilterModel("1", "Falcon 1", true),
            RocketFilterModel("2", "Falcon 2", true),
            RocketFilterModel("3", "Falcon 3", false),
            RocketFilterModel("4", "Falcon 4", false),
            RocketFilterModel("5", "Falcon 5", true),
        ).toPersistentList(),
        onCheckedAll = {},
        onCheckedChange = {},
        onBackClick = {},
    )
}
