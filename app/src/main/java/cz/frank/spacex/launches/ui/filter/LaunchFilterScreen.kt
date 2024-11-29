package cz.frank.spacex.launches.ui.filter

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.frank.spacex.R
import org.koin.androidx.compose.koinViewModel

@Composable fun LaunchesFilterScreen(
    onBackClick: () -> Unit,
    navigateToRockets: () -> Unit,
    modifier: Modifier = Modifier,
    vm: LaunchFilterViewModel = koinViewModel()
) {
    val launchedChip by vm.launchedChip.collectAsState()
    val upcomingChip by vm.upcomingChip.collectAsState()
    val rocketsCount by vm.rocketsCount.collectAsState()
    LaunchesFilterScreenLayout(
        isAlreadyLaunchedSelectedChip = launchedChip,
        isUpcomingSelectedChip = upcomingChip,
        selectedRockets = rocketsCount,
        onRocketsClick = navigateToRockets,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LaunchesFilterScreenLayout(
    isAlreadyLaunchedSelectedChip: Chip?,
    isUpcomingSelectedChip: Chip?,
    selectedRockets: Int,
    onRocketsClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.launches_filter_title)) },
                navigationIcon = {
                    IconButton(onBackClick) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                }
            )
        },
        modifier = modifier
    ) {
        Column(
            Modifier.padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UpcomingLaunchesFilter(isAlreadyLaunchedSelectedChip, isUpcomingSelectedChip)
            HorizontalDivider()
            RocketsFilter(selectedRockets, onRocketsClick)
            HorizontalDivider()
        }
    }
}

data class Chip(val isSelected: Boolean, val onClick: () -> Unit)

@Composable private fun UpcomingLaunchesFilter(
    isAlreadyLaunchedSelectedChip: Chip?,
    isUpcomingSelectedChip: Chip?,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        isAlreadyLaunchedSelectedChip?.let {
            FilterChip(
                it.isSelected,
                it.onClick,
                R.string.launches_filter_chip_launched
            )
        }

        isUpcomingSelectedChip?.let {
            FilterChip(
                it.isSelected,
                it.onClick,
                R.string.launches_filter_chip_upcoming
            )
        }
    }
}

@Composable private fun RocketsFilter(selectedRockets: Int, onClick: () -> Unit) {
    Row(
        Modifier
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.launches_filter_rockets),
            style = MaterialTheme.typography.labelLarge

        )
        Spacer(Modifier.weight(1f))
        if (selectedRockets != 0) {
            Badge(Modifier.padding(16.dp)) {
                Text(selectedRockets.toString())
            }
        }
        Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, null)
    }
}

@Composable private fun FilterChip(
    isSelected: Boolean,
    onClick: () -> Unit,
    @StringRes label: Int,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(stringResource(label)) },
        leadingIcon = { AnimatedVisibility(isSelected) { Icon(Icons.Default.Check, null) } },
        trailingIcon = { AnimatedVisibility(!isSelected) { Icon(Icons.Default.Close, null) } }
    )
}

@Preview
@Composable
private fun NoRocketsSelectedPreview() {
    LaunchesFilterScreenLayout(
        Chip(false) {},
        Chip(true) {},
        selectedRockets = 0,
        onRocketsClick = {},
        onBackClick = {}
    )
}

@Preview
@Composable
private fun RocketsSelectedPreview() {
    LaunchesFilterScreenLayout(
        Chip(false) {},
        Chip(true) {},
        selectedRockets = 3,
        onRocketsClick = {},
        onBackClick = {}
    )
}
