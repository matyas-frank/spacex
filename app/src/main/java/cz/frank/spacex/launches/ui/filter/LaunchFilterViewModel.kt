package cz.frank.spacex.launches.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LaunchFilterViewModel(private val filterRepository: ILaunchesFilterRepository) : ViewModel() {
    val launchedChip: StateFlow<Chip?> = filterRepository.isLaunchedSelected
        .map { Chip(it, ::toggleLaunchSelected) }
        .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), null)

    val upcomingChip: StateFlow<Chip?> = filterRepository.isUpcomingSelected
            .map { Chip(it, ::toggleUpcomingSelected) }
            .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), null)

    val rocketsCount = filterRepository.rocketsCount
        .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5000), 0)

    fun toggleLaunchSelected() = viewModelScope.launch {
        if (launchedChip.value?.isSelected == true) {
            if (upcomingChip.value?.isSelected == true) {
                filterRepository.toggleLaunchedSelected()
            }
        } else {
            filterRepository.toggleLaunchedSelected()
        }
    }

    fun toggleUpcomingSelected() = viewModelScope.launch {
        if (upcomingChip.value?.isSelected == true) {
            if (launchedChip.value?.isSelected == true) {
                filterRepository.toggleUpcomingSelected()
            }
        } else {
            filterRepository.toggleUpcomingSelected()
        }
    }
}
