package cz.frank.spacex.launches.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fresh.materiallinkpreview.models.OpenGraphMetaData
import com.fresh.materiallinkpreview.parsing.OpenGraphMetaDataProvider
import cz.frank.spacex.launches.data.repository.LaunchesRepository
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
    private val repository: LaunchesRepository by inject()
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
                    val res = repository.detailLaunch(launchId)
                    _launch.value =  res
                    res.onSuccess {
                        it.article?.let { _article.value = getLinkMetadata(it) }
                    }
                }
            }
        }

    }

    private suspend fun getLinkMetadata(link: String): Result<OpenGraphMetaData> {
        val openGraphMetaDataProvider = OpenGraphMetaDataProvider()
        return openGraphMetaDataProvider.startFetchingMetadataAsync(URL(link))
    }
}
