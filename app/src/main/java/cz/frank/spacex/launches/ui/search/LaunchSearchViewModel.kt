package cz.frank.spacex.launches.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cz.frank.spacex.launches.data.repository.ILaunchesFilterRepository
import cz.frank.spacex.launches.data.repository.LaunchesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class LaunchSearchViewModel(
    private val filterRepository: ILaunchesFilterRepository,
    launchesRepository: LaunchesRepository,
) : ViewModel() {
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

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pager = filterRepository
        .allFilters
        .distinctUntilChanged()
        .debounce(400.milliseconds)
        .flatMapLatest(launchesRepository::pager)
        .cachedIn(viewModelScope)

    fun onQueryChange(query: String) {
        _query.value = query
        saveQuery()
    }
    fun eraseQuery() = onQueryChange("")

    private fun saveQuery() {
       viewModelScope.launch { filterRepository.setQuery(query.value) }
    }

    companion object {
        fun isQueryEmpty(query: String) = query.isBlank()
    }
}
