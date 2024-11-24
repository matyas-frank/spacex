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
    selectedDrawerSection: DrawerItem,
    modifier: Modifier = Modifier,
    onItemClick: (DrawerItem) -> Unit
) {
    ModalDrawerSheet(modifier) {
        Header()
        HorizontalDivider(Modifier.padding(vertical = 16.dp))
        DrawerItems(onItemClick, selectedDrawerSection)
    }
}

@Composable private fun Header() {
    Text("SPACEX", modifier = Modifier.padding(16.dp))
}

@Composable private fun DrawerItems(onItemClick: (DrawerItem) -> Unit, selectedDrawerSection: DrawerItem) {
    Column {
        drawerItems.forEach {
            DrawerItem(it, it.id == selectedDrawerSection, onItemClick)
        }
    }
}

@Composable private fun DrawerItem(item: DrawerItemUI, isSelected: Boolean, onItemClick: (DrawerItem) -> Unit) {
    NavigationDrawerItem(
        label = { Text(stringResource(item.name)) },
        selected = isSelected,
        onClick = { onItemClick(item.id) },
        modifier = Modifier.padding(horizontal = 16.dp),
        icon = { Icon(painterResource(item.icon), null) }
    )
}

private val drawerItems = listOf(
    DrawerItemUI(DrawerItem.Dragons, R.string.drawer_dragons, R.drawable.ic_rocket),
    DrawerItemUI(DrawerItem.Starlink, R.string.drawer_starlink, R.drawable.ic_satellite)
)

private data class DrawerItemUI(val id: DrawerItem, @StringRes val name: Int, @DrawableRes val icon: Int)

enum class DrawerItem {
    Dragons, Starlink
}