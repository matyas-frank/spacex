package cz.frank.spacex.launches.ui.filter

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable fun LaunchesFilterScreen(modifier: Modifier = Modifier) {
    Text("LaunchFilterScreen", modifier)
}

@Composable private fun Chip(
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
