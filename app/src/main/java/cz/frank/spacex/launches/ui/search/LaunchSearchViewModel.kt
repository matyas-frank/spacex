package cz.frank.spacex.launches.ui.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LaunchSearchViewModel : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    fun onQueryChange(query: String) {
        _query.value = query
    }
    fun isQueryEmpty(query: String) = query.isBlank()
    fun eraseQuery() = onQueryChange("")
}
