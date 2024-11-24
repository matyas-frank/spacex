package cz.frank.spacex.starlink.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable fun StarlinkMap(toggleDrawer: () -> Unit, modifier: Modifier = Modifier, ) {
    Scaffold(Modifier.fillMaxSize()) {
        Surface(Modifier.padding(it)) {
            Text("Starlink", modifier.clickable { toggleDrawer() })
        }
    }
}
