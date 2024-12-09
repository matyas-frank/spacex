package cz.frank.spacex.launches.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fresh.materiallinkpreview.models.OpenGraphMetaData
import com.fresh.materiallinkpreview.parsing.OpenGraphMetaDataProvider
import cz.frank.spacex.launches.data.repository.ILaunchesRepository
import kotlinx.coroutines.Job
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
    private val _launch = MutableStateFlow<Result<LaunchDetailModel>?>(null)
    val launch = _launch.asStateFlow()

    private val _article = MutableStateFlow<Result<OpenGraphMetaData>?>(null)
    val article = _article.asStateFlow()

    private var syncingJob: Job? = null
    private val syncMutex = Mutex()

    init { fetchLaunch() }

    fun fetchLaunch() {
        viewModelScope.launch {
            syncMutex.withLock {
                if (syncingJob == null) syncingJob = viewModelScope.launch {
                    _launch.value = null
                    _launch.value = repository.detailLaunch(launchId).also { result ->
                        result.onSuccess {
                            it.article?.let { _article.value = getLinkMetadata(it) }
                        }
                        syncingJob = null
                    }
                }
            }
        }
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
}
