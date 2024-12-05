package cz.frank.spacex.launches.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.fresh.materiallinkpreview.models.OpenGraphMetaData
import com.fresh.materiallinkpreview.ui.CardLinkPreview
import com.fresh.materiallinkpreview.ui.CardLinkPreviewProperties
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import cz.frank.spacex.R
import cz.frank.spacex.shared.ui.theme.attentionColor
import cz.frank.spacex.shared.ui.theme.failureColor
import cz.frank.spacex.shared.ui.theme.successColor
import kotlinx.datetime.*
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.parcelize.Parcelize


@Composable fun LaunchDetailScreen(
    launchId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    vm: LaunchDetailViewModel = viewModel(key = launchId) { LaunchDetailViewModel(launchId) }
) {
    val item by vm.launch.collectAsStateWithLifecycle()
    val article by vm.article.collectAsStateWithLifecycle()
    LaunchDetailScreenLayout(
        onBackClick,
        item,
        article,
        vm::fetchLaunch,
        modifier,
    )
}

@Composable private fun LaunchDetailScreenLayout(
    onBackClick: () -> Unit,
    model: Result<LaunchDetailModel>?,
    article: Result<OpenGraphMetaData>?,
    retry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    model?.let { model ->
        model.onSuccess {
            SuccessScreen(it, article, onBackClick, modifier)
        }.onFailure {
            FailureScreen(retry = retry)
        }
    } ?: LoadingScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun SuccessScreen(
    model: LaunchDetailModel,
    articleMetaData: Result<OpenGraphMetaData>?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = { Text("${model.flightNumber}.${model.name}") },
                navigationIcon = {
                    IconButton({ onBackClick() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                }
            )
        },
    ) {
        Column(
            Modifier.padding(it)) {
            model.youtubeId?.let {
                YoutubePlayer(it)
            }
            val scroll = rememberScrollState()
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(scroll)) {
                model.launchpad?.let {
                    IconTextSection(R.drawable.ic_pin_drop, it.name)
                    HorizontalDivider()
                }

                model.rocket.let {
                    IconTextSection(R.drawable.ic_rocket, it.name)
                    HorizontalDivider()
                }
                IconTextSection(
                    R.drawable.ic_calendar_month,
                    Instant
                        .fromEpochMilliseconds(model.date)
                        .format(stringResource(R.string.launches_detail_date_format))
                )
                HorizontalDivider()
                model.detail?.let {
                    IconTextSection(R.drawable.ic_description, it)
                    HorizontalDivider()
                }
                Indicators(model)

                model.article?.let {
                    HorizontalDivider()
                    Box(Modifier.padding(vertical = 16.dp)) {
                        Article(it, articleMetaData)
                    }
                }
            }
        }
    }
}

@Composable private fun YoutubePlayer(youtubeId: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(factory = {
        YouTubePlayerView(context).apply {
            lifecycleOwner.lifecycle.addObserver(this)
            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.cueVideo(youtubeId, 0f)
                }
            })
        }
    })
}

@Composable private fun Indicators(model: LaunchDetailModel) {
    Row(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconSize = 28.dp
        TextWithSuccessIcon(R.string.launches_detail_launched) {
            LaunchIcon(iconSize, model.state)
        }
        model.fairingsRecovered?.let { recovered ->
            Spacer(Modifier.weight(1f))
            TextWithSuccessIcon(R.string.launches_detail_fairings_recovered) {
                LandingIcon(recovered, iconSize)
            }
        }
    }
}

@OptIn(FormatStringsInDatetimeFormats::class)
private fun Instant.format(format: String) = toLocalDateTime(TimeZone.currentSystemDefault())
        .format(LocalDateTime.Format { byUnicodePattern(format) })

@Composable private fun IconTextSection(icon: Int, text: String, onClick: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
        .run {
            onClick?.let { clickable { it } } ?: Modifier
        }
        .padding(16.dp)) {
        Icon(painterResource(icon), null)
        Spacer(Modifier.width(16.dp))
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
        onClick?.let {
            Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, null)
        }
    }
}

@Composable private fun TextWithSuccessIcon(text: Int, icon: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 16.dp)) {
        Text(stringResource(text), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(16.dp))
        icon()
    }
}

@Composable private fun LaunchIcon(iconSize: Dp, state: LaunchDetailModel.State) {
    val color = when (state) {
        LaunchDetailModel.State.Upcoming -> attentionColor
        is LaunchDetailModel.State.Launched -> when (state.wasSuccessful) {
            true -> successColor
            false -> failureColor
            null -> attentionColor
        }
    }
    IconCardHolder(color) {
        when (state) {
            is LaunchDetailModel.State.Upcoming -> {
                val extraPadding = 2.dp
                Icon(
                    painterResource(R.drawable.ic_event_upcoming),
                    null,
                    Modifier
                        .padding(extraPadding)
                        .size(iconSize - extraPadding)
                )
            }

            is LaunchDetailModel.State.Launched -> {
                val modifier = Modifier.size(iconSize)
                state.wasSuccessful?.let {
                    Icon(
                        if (state.wasSuccessful) Icons.Default.Check else Icons.Default.Close,
                        null,
                        modifier
                    )
                } ?: Icon(painterResource(R.drawable.ic_question_mark), null, modifier)
            }
        }
    }
}

@Composable private fun IconCardHolder(color: Color, content: @Composable () -> Unit) {
    Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = color)) {
        Box(Modifier.padding(8.dp)) {
            content()
        }
    }
}

@Composable private fun LandingIcon(recovered: Boolean, size: Dp) {
    IconCardHolder(if (recovered) successColor else failureColor) {
        Icon(
            if (recovered) Icons.Default.Check else Icons.Default.Close,
            null,
            Modifier.size(size)
        )
    }
}

@Composable private fun Article(article: String, metaData: Result<OpenGraphMetaData>?)  {
    metaData?.let {
        it.onSuccess {
            CardLinkPreview(
                it, CardLinkPreviewProperties.Builder(
                    imagePainter = rememberAsyncImagePainter(it.imageUrl),
                    drawWithCardOutline = true,
                    maxNumberOfLinesForTitle = 3,
                    maxNumberOfLinesForDescription = 4

                ).build()
            )
        }.onFailure {
            NotLoadedLinkMetaData(article)
        }
    } ?: NotLoadedLinkMetaData(article)

}

@Composable private fun NotLoadedLinkMetaData(url: String?) {
    val context = LocalContext.current
    Card(onClick = {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.launches_detail_link), Modifier.padding(16.dp))
        }
    }
}

@Composable private fun FailureScreen(modifier: Modifier = Modifier, retry: () -> Unit) {
    Scaffold(modifier) {
        Box(
            Modifier
                .padding(it)
                .fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(retry) { Text(stringResource(R.string.launch_search_retry_button)) }
        }
    }
}

@Composable private fun LoadingScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier) {
        Box(
            Modifier
                .padding(it)
                .fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

}

@Parcelize data class LaunchDetail(val id: String) : Parcelable
