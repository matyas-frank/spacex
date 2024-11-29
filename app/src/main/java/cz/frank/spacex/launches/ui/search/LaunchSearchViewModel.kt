package cz.frank.spacex.launches.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LaunchSearchViewModel(private val filterRepository: ILaunchesFilterRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val isAnyFilterActive = filterRepository.isAnyFilterActive
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            false
        )

    init {
        viewModelScope.launch { _query.value = filterRepository.query.first() }
    }

    fun onQueryChange(query: String) {
        _query.value = query
        triggerSearch()
    }
    fun eraseQuery() = onQueryChange("")

    private fun triggerSearch() {
       viewModelScope.launch { filterRepository.setQuery(query.value) }
    }

    companion object {
        fun isQueryEmpty(query: String) = query.isBlank()
    }
}
