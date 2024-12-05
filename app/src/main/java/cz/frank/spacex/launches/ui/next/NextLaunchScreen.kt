package cz.frank.spacex.launches.ui.next

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.frank.spacex.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.seconds

@Composable fun NextLaunchScreen(
    navigateDetail: (String) -> Unit,
    toggleDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    vm: NextLaunchViewModel = koinViewModel()
) {
    val countDown by vm.countDown.collectAsStateWithLifecycle()
    val launch by vm.launch.collectAsStateWithLifecycle()
    LaunchedEffect(launch) {
        launch?.let {
            while (isActive) {
                vm.calcRemainingTime()
                delay(1.seconds)
            }
        }
    }
    launch?.let {
        NextLaunchScreenLayout(it, countDown, toggleDrawer, navigateDetail, modifier)
    }

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NextLaunchScreenLayout(
    launch: NextLaunchModel,
    countDown: CountDown?,
    toggleDrawer: () -> Unit,
    navigateDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier.fillMaxSize(), topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.next_launch_title)) },
            navigationIcon = { IconButton(onClick = toggleDrawer) { Icon(Icons.Default.Menu, null)  } }
        )
    }) {
        Column(Modifier.padding(it)) {
            countDown?.let {
                CountDown(countDown)
            }
            Info(launch, navigateDetail)
        }
    }
}

@Composable private fun CountDown(countDown: CountDown) {
    Surface {
        LazyVerticalGrid(GridCells.Fixed(2)) {
            item { PropertyCountWithLabel(countDown.days, R.string.next_launch_days) }
            item { PropertyCountWithLabel(countDown.hours, R.string.next_launch_hours) }
            item { PropertyCountWithLabel(countDown.minutes, R.string.next_launch_minutes) }
            item { PropertyCountWithLabel(countDown.seconds, R.string.next_launch_seconds) }
        }
    }
}

@Composable private fun PropertyCountWithLabel(count: Long, label: Int) {
    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            count.toString()
                .mapIndexed { index, c -> Digit(c, count, index) }
                .forEach { digit ->
                    AnimatedContent(
                        targetState = digit,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInVertically { -it } togetherWith slideOutVertically { it }
                            } else {
                                slideInVertically { it } togetherWith slideOutVertically { -it }
                            }
                        }
                    ) { digit ->
                        Text(
                            "${digit.digitChar}",
                            style = MaterialTheme.typography.displayLarge,
                        )
                    }
                }
        }
        Spacer(Modifier.height(4.dp))
        Text(stringResource(label), style = MaterialTheme.typography.headlineSmall)
    }
}

data class Digit(val digitChar: Char, val fullNumber: Long, val place: Int) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Digit -> digitChar == other.digitChar
            else -> super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = digitChar.hashCode()
        result = 31 * result + fullNumber.toInt()
        result = 31 * result + place
        return result
    }
}

operator fun Digit.compareTo(other: Digit): Int {
    return fullNumber.compareTo(other.fullNumber)
}

@Composable private fun Info(launch: NextLaunchModel, navigateDetail: (String) -> Unit) {
    Card(
        onClick = { navigateDetail(launch.id) },
        Modifier
            .padding(32.dp)
            .fillMaxWidth()
            .heightIn(150.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(launch.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(launch.rocket)
            Text(launch.launchpad)
            Text(launch.date)
        }
    }
}

data class CountDown(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)

@Preview
@Composable
@Suppress("MagicNumber")
private fun Preview() {
    NextLaunchScreenLayout(
        NextLaunchModel(
        "1",
        "Sat1",
        "Falcon1",
        "Caneveral",
        LocalDateTime(2024, 12, 5, 17, 0, 0)
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds(),
        "5.12.2024 17:00"
    ), CountDown(10, 20, 30, 40), {}, { }
    )
}
