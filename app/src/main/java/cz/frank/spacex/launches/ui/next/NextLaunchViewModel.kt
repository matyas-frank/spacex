package cz.frank.spacex.launches.ui.next

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.*

@Suppress("MagicNumber")
class NextLaunchViewModel: ViewModel() {
    private val _launch = MutableStateFlow<NextLaunchModel?>(null)
    val launch = _launch.asStateFlow()

    private val _remainingTime = MutableStateFlow<CountDown?>(null)
    val countDown = _remainingTime.asStateFlow()

    init {
        _launch.value = NextLaunchModel(
            "633f72000531f07b4fdf59c2",
            "Sat1",
            "Falcon1",
            "Caneveral",
            LocalDateTime(2024, 12, 5, 17, 0, 0)
                .toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds(),
            date = "12.5.2024 17:00"
        )
    }

    fun calcRemainingTime() {
        launch.value?.epoch?.let { epoch ->
            val now = Clock.System.now()
            val launch = Instant.fromEpochMilliseconds(epoch)
            val difference = launch - now
            difference.toComponents { days, hours, minutes, seconds, _ ->
                _remainingTime.value = CountDown(days, hours.toLong(), minutes.toLong(), seconds.toLong())
            }
        }

    }
}

data class NextLaunchModel(
    val id: String,
    val name: String,
    val rocket: String,
    val launchpad: String,
    val epoch: Long,
    val date: String
)
