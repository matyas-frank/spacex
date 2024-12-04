package cz.frank.spacex.launches.ui.filter.rocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRocketRepository
import cz.frank.spacex.launches.data.repository.RocketFilterModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LaunchFilterRocketViewModel(private val filterRocketRepository: ILaunchesFilterRocketRepository) : ViewModel() {
    private val _rockets = MutableStateFlow(listOf<RocketFilterModel>())
    val rockets = _rockets.asStateFlow()

    private var arePrefsFetched = false
    private var backupRockets = listOf<RocketFilterModel>()

    init {
       viewModelScope.launch { updateRockets() }
    }

    fun onCheckedChange(id: String) {
        _rockets.update { rockets ->
            if (!(rockets.count { it.isSelected } == 1 && rockets.find { it.id == id }!!.isSelected)) {
                rockets.map { if (it.id == id) it.copy(isSelected = !it.isSelected) else it }
            } else rockets
        }
    }

    fun checkedAllClick() {
        _rockets.update { rockets ->
            if (rockets.all { it.isSelected }) {
                rockets.mapIndexed { index, rocket ->
                    if (index == 0) rocket.copy(isSelected = true)
                    else rocket.copy(isSelected = false)
                }
            } else rockets.map { it.copy(isSelected = true) }
        }
    }

    private suspend fun updateRockets() {
        backupRockets = filterRocketRepository.getRockets()
        _rockets.update {
            if (backupRockets.none { it.isSelected }) {
                backupRockets.map { it.copy(isSelected = true) }
            } else backupRockets
        }
        arePrefsFetched = true
    }

    fun saveRockets() {
        val rocketsToSave = rockets.value
        if (arePrefsFetched && rocketsToSave != backupRockets) {
            viewModelScope.launch {
                filterRocketRepository.saveRockets(
                    if (rocketsToSave.all { it.isSelected }) setOf()
                    else rocketsToSave.filter { it.isSelected }.map { it.id }.toSet()
                )
            }
        }
    }
}
