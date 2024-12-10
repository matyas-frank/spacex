package cz.frank.spacex.launches.ui.filter.rocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRocketRepository
import cz.frank.spacex.launches.data.repository.RocketFilterModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LaunchFilterRocketViewModel(private val filterRocketRepository: ILaunchesFilterRocketRepository) : ViewModel() {
    private val _rockets = MutableStateFlow<ImmutableList<RocketFilterModel>>(persistentListOf())
    val rockets = _rockets.asStateFlow()

    private var arePrefsFetched = false
    private var initiallyLoadedRockets = listOf<RocketFilterModel>()

    init {
       viewModelScope.launch { updateRockets() }
    }

    fun onCheckedChange(id: String) {
        _rockets.update { rockets ->
            if (!(rockets.count { it.isSelected } == 1 && rockets.find { it.id == id }!!.isSelected)) {
                rockets.map { if (it.id == id) it.copy(isSelected = !it.isSelected) else it }.toImmutableList()
            } else rockets
        }
    }

    fun checkedAllClick() {
        _rockets.update { rockets ->
            if (rockets.all { it.isSelected }) {
                rockets.mapIndexed { index, rocket ->
                    if (index == 0) rocket.copy(isSelected = true)
                    else rocket.copy(isSelected = false)
                }.toImmutableList()
            } else rockets.map { it.copy(isSelected = true) }.toImmutableList()
        }
    }

    private suspend fun updateRockets() {
        initiallyLoadedRockets = filterRocketRepository.getRockets()
        _rockets.update {
            if (initiallyLoadedRockets.none { it.isSelected }) {
                initiallyLoadedRockets.map { it.copy(isSelected = true) }.toImmutableList()
            } else initiallyLoadedRockets.toImmutableList()
        }
        arePrefsFetched = true
    }

    fun saveRockets() {
        val rocketsToSave = rockets.value
        if (arePrefsFetched && rocketsToSave != initiallyLoadedRockets) {
            viewModelScope.launch {
                filterRocketRepository.saveRockets(
                    if (rocketsToSave.all { it.isSelected }) setOf()
                    else rocketsToSave.filter { it.isSelected }.map { it.id }.toSet()
                )
            }
        }
    }
}
