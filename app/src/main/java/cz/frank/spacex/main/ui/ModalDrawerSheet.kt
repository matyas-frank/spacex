package cz.frank.spacex.main.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cz.frank.spacex.R


@Composable fun ModalDrawerSheet(
    isDrawerItemSelected: (NavigationDrawerItem) -> Boolean,
    modifier: Modifier = Modifier,
    onItemClick: (NavigationDrawerItem) -> Unit
) {
    ModalDrawerSheet(modifier) {
        Header()
        HorizontalDivider(Modifier.padding(vertical = 16.dp))
        DrawerItems(onItemClick, isDrawerItemSelected)
    }
}

@Composable private fun Header() {
    Text("SPACEX", modifier = Modifier.padding(16.dp))
}

@Composable private fun DrawerItems(
    onItemClick: (NavigationDrawerItem) -> Unit,
    isDrawerItemSelected: (NavigationDrawerItem) -> Boolean
) {
    Column {
        drawerItems.forEach {
            DrawerItem(it, isDrawerItemSelected(it.destination), onItemClick)
        }
    }
}

@Composable private fun DrawerItem(
    item: DrawerItemUI,
    isSelected: Boolean,
    onItemClick: (NavigationDrawerItem) -> Unit
) {
    NavigationDrawerItem(
        label = { Text(stringResource(item.name)) },
        selected = isSelected,
        onClick = { onItemClick(item.destination) },
        modifier = Modifier.padding(horizontal = 16.dp),
        icon = { Icon(painterResource(item.icon), null) }
    )
}

private val drawerItems = listOf(
    DrawerItemUI(
        R.string.drawer_launches,
        R.drawable.ic_rocket,
        NavigationDrawerItem.Launches
    ),
    DrawerItemUI(
        R.string.drawer_starlink,
        R.drawable.ic_satellite,
        NavigationDrawerItem.Starlink
    )
)

private data class DrawerItemUI(
    @StringRes val name: Int,
    @DrawableRes val icon: Int,
    val destination: NavigationDrawerItem
)

