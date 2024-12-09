package cz.frank.spacex.launches.ui.detail

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fresh.materiallinkpreview.models.OpenGraphMetaData
import com.fresh.materiallinkpreview.parsing.OpenGraphMetaDataProvider
import cz.frank.spacex.R
import cz.frank.spacex.launches.data.repository.ILaunchesRepository
import cz.frank.spacex.launches.domain.model.LaunchDetailModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URL

class LaunchDetailViewModel(private val launchId: String) : ViewModel(), KoinComponent {
    private val repository: ILaunchesRepository by inject()
    private val application: Application by inject()
    private val _launch = MutableStateFlow<Result<LaunchDetailModel>?>(null)
    val launch = _launch.asStateFlow()

    private val _article = MutableStateFlow<Result<OpenGraphMetaData>?>(null)
    val article = _article.asStateFlow()

    private var syncingJob: Job? = null
    private val syncMutex = Mutex()

    private val _isPullRefreshing = MutableStateFlow(false)
    val isPullRefreshing = _isPullRefreshing.asStateFlow()

    init { fetchLaunch() }

    fun fetchLaunch() {
        fetchLaunch(
            beforeFetch = { _launch.value = null },
            withResult = { _launch.value = it }
        )
    }

    private fun fetchLaunch(
        beforeFetch: () -> Unit,
        withResult: suspend (Result<LaunchDetailModel>) -> Unit
    ) {
        viewModelScope.launch {
            syncMutex.withLock {
                if (syncingJob == null) syncingJob = viewModelScope.launch {
                    beforeFetch()
                    withResult(repository.detailLaunch(launchId).also { result ->
                        result.onSuccess {
                            it.article?.let { _article.value = getLinkMetadata(it) }
                        }
                    })
                    syncingJob = null
                }
            }
        }
    }

    fun pullRefresh() {
        fetchLaunch(
            beforeFetch = { _isPullRefreshing.value = true },
            withResult = {
                it.onSuccess {
                    _isPullRefreshing.value = false
                    _launch.value = Result.success(it)
                }
                it.onFailure {
                    delay(DELAY_NEEDED_FOR_CORRECT_ANIMATION_OF_PULL_REFRESH)
                    _isPullRefreshing.value = false
                    Toast.makeText(application, R.string.fetch_problem, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }


    private var playedTime = 0f
    fun savedYoutubeVideoElapsedTime(playedTime: Float) {
        this.playedTime = playedTime
    }

    fun getYoutubeVideoElapsedTime() = playedTime

    private suspend fun getLinkMetadata(link: String): Result<OpenGraphMetaData> {
        val openGraphMetaDataProvider = OpenGraphMetaDataProvider()
        return openGraphMetaDataProvider.startFetchingMetadataAsync(URL(link))
    }

    private companion object {
        const val DELAY_NEEDED_FOR_CORRECT_ANIMATION_OF_PULL_REFRESH = 100L
    }
}
