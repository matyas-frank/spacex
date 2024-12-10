package cz.frank.spacex.crew.ui.search

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.frank.spacex.R
import cz.frank.spacex.crew.data.CrewRepository
import cz.frank.spacex.crew.domain.model.CrewMemberModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CrewSearchViewModel(
    private val crewRepository: CrewRepository,
    private val application: Application
) : ViewModel() {
    private val _members = MutableStateFlow<Result<ImmutableList<CrewMemberModel>>?>(null)
    val members = _members.asStateFlow()

    private var syncingJob: Job? = null
    private val syncMutex = Mutex()

    private val _isPullRefreshing = MutableStateFlow(false)
    val isPullRefreshing = _isPullRefreshing.asStateFlow()

    init { fetchCrew() }

    fun fetchCrew() {
        fetchCrew(
            beforeFetch = { _members.value = null },
            withResult = { _members.value = it }
        )
    }

    private fun fetchCrew(
        beforeFetch: () -> Unit,
        withResult: suspend (Result<ImmutableList<CrewMemberModel>>) -> Unit
    ) {
        viewModelScope.launch {
            syncMutex.withLock {
                if (syncingJob == null) syncingJob = viewModelScope.launch {
                    beforeFetch()
                    withResult(crewRepository.fetchCrew().mapCatching { it.toImmutableList() })
                    syncingJob = null
                }
            }
        }
    }

    fun pullRefresh() {
        fetchCrew(
            beforeFetch = { _isPullRefreshing.value = true },
            withResult = {
                delay(DELAY_NEEDED_FOR_CORRECT_ANIMATION_OF_PULL_REFRESH)
                it.onSuccess {
                    _isPullRefreshing.value = false
                    _members.value = Result.success(it)
                }
                it.onFailure {
                    _isPullRefreshing.value = false
                    Toast.makeText(application, R.string.fetch_problem, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private companion object {
        const val DELAY_NEEDED_FOR_CORRECT_ANIMATION_OF_PULL_REFRESH = 100L
    }
}

