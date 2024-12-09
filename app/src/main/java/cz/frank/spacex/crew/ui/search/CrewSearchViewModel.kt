package cz.frank.spacex.crew.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.frank.spacex.crew.data.CrewRepository
import cz.frank.spacex.crew.domain.model.CrewMemberModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CrewSearchViewModel(private val crewRepository: CrewRepository) : ViewModel() {
    private val _members = MutableStateFlow<Result<ImmutableList<CrewMemberModel>>?>(null)
    val members = _members.asStateFlow()

    private var syncingJob: Job? = null
    private val syncMutex = Mutex()

    init { fetchCrew() }

    fun fetchCrew() {
        viewModelScope.launch {
            syncMutex.withLock {
                if (syncingJob == null) syncingJob = viewModelScope.launch {
                    _members.value = null
                    _members.value = crewRepository.fetchCrew().mapCatching { it.toImmutableList() }
                    syncingJob = null
                }
            }
        }
    }
}
