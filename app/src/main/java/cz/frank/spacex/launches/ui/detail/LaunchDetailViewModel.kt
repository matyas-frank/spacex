package cz.frank.spacex.launches.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fresh.materiallinkpreview.models.OpenGraphMetaData
import com.fresh.materiallinkpreview.parsing.OpenGraphMetaDataProvider
import cz.frank.spacex.launches.data.repository.LaunchesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URL
import kotlin.time.Duration.Companion.seconds

class LaunchDetailViewModel(private val launchId: String) : ViewModel(), KoinComponent {
    private val repository: LaunchesRepository by inject()
    private val _launch = MutableStateFlow<Result<LaunchDetailModel>?>(null)
    val launch = _launch.asStateFlow()

    private val _article = MutableStateFlow<Result<OpenGraphMetaData>?>(null)
    val article = _article.asStateFlow()

    init { fetchLaunch() }

    fun fetchLaunch() {
        viewModelScope.launch {
            val res = repository.detailLaunch(launchId)
            _launch.value =  res
            res.onSuccess {
                it.article?.let { _article.value = getLinkMetadata(it) }
            }
        }
    }

    private suspend fun getLinkMetadata(link: String): Result<OpenGraphMetaData> {
        val openGraphMetaDataProvider = OpenGraphMetaDataProvider()
        return openGraphMetaDataProvider.startFetchingMetadataAsync(URL(link))
    }
}
