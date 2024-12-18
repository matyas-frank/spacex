package cz.frank.spacex.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter.State
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import cz.frank.spacex.R
import kotlinx.coroutines.delay


@Composable fun CachedRemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onState: ((State) -> Unit)? = null,
) {
    if (url != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .memoryCacheKey(url)
                .diskCacheKey(url)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            onState = onState,
        )
    }
}

private const val TIME_NEEDED_TO_REFRESH_IN_MILLIS = 400L

@Composable fun RefreshableCachedImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    var imageState by remember { mutableStateOf<State?>(null) }
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            delay(TIME_NEEDED_TO_REFRESH_IN_MILLIS)
            isVisible = true
        }
    }

    Crossfade(isVisible) {
        if (it) {
            CachedRemoteImage(url, contentDescription, modifier, contentScale, onState = { imageState = it })
            AnimatedVisibility(imageState !is State.Success, enter = fadeIn(), exit = fadeOut()) {
                when (imageState) {
                    is State.Success, State.Empty -> {}
                    is State.Error -> { Error({ isVisible = false }) }
                    is State.Loading, null -> { Loading() }
                }
            }
        }
    }
}

@Composable private fun Error(onRefresh: () -> Unit) {
    Box(
        Modifier
            .padding(32.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(onRefresh) {
            Text(stringResource(R.string.launch_search_refresh_button))
        }
    }
}

@Composable private fun Loading() {
    Box(
        Modifier
            .padding(32.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) { CircularProgressIndicator(Modifier) }
}
