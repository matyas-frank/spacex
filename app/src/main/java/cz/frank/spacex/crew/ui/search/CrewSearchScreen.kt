package cz.frank.spacex.crew.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable fun CrewSearchScreen(
    navHostController: NavHostController,
    toggleDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(Modifier.fillMaxSize()) {
        Surface(Modifier.padding(it)) {
            Text("Crew", modifier.clickable { toggleDrawer() })
        }
    }
}
