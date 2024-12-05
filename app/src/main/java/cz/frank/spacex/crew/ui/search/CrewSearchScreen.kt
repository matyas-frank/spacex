package cz.frank.spacex.crew.ui.search

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import cz.frank.spacex.R
import cz.frank.spacex.crew.domain.model.CrewMemberModel
import cz.frank.spacex.shared.ui.theme.SpaceXTheme
import cz.frank.spacex.shared.ui.theme.attentionColor
import cz.frank.spacex.shared.ui.theme.failureColor
import cz.frank.spacex.shared.ui.theme.successColor
import org.koin.androidx.compose.koinViewModel

@Composable fun CrewSearchScreen(
    toggleDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    vm: CrewSearchViewModel = koinViewModel()
) {
    val membersResult by vm.members.collectAsStateWithLifecycle()
    CrewSearchScreenLayout(membersResult, vm::fetchCrew, toggleDrawer, modifier)
}

@Composable private fun CrewSearchScreenLayout(
    membersResult: Result<List<CrewMemberModel>>?,
    retry: () -> Unit,
    toggleDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        membersResult?.let { model ->
            model.onSuccess {
                CrewSearchSuccessLayout(it, toggleDrawer)
            }.onFailure {
                FailureScreen(retry = retry)
            }
        } ?: LoadingScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun CrewSearchSuccessLayout(members: List<CrewMemberModel>, toggleDrawer: () -> Unit) {
    Scaffold(
        Modifier.fillMaxSize(),
        topBar = { TopAppBar(
            title = { Text(stringResource(R.string.crew_search_title)) },
            navigationIcon = { IconButton(toggleDrawer) { Icon(Icons.Default.Menu, null) } }
        ) }
    ) {
        CrewMembers(members, Modifier.padding(it))
    }
}

@Composable private fun CrewMembers(members: List<CrewMemberModel>, modifier: Modifier = Modifier) {
    LazyColumn(modifier) {
        items(members) {
            CrewMember(it)
        }
    }
}

@Composable private fun CrewMember(member: CrewMemberModel) {
    val context = LocalContext.current
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Card(
            onClick = {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(member.link))
                context.startActivity(browserIntent)
            },
            Modifier.padding(vertical = 16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(member.name, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(16.dp))
                    ActiveIndicator(member.status)
                }
                Spacer(Modifier.padding(8.dp))
                val painter = rememberAsyncImagePainter(member.image)
                Card(shape = RoundedCornerShape(22.dp)) {
                    Image(
                        painter,
                        null,
                        Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth)
                }
            }
        }
    }
}

val CrewMemberModel.Status.color get() = when (this) {
    CrewMemberModel.Status.ACTIVE -> successColor
    CrewMemberModel.Status.INACTIVE -> attentionColor
    CrewMemberModel.Status.RETIRED -> failureColor
    CrewMemberModel.Status.UNKNOWN -> Color.Gray
}

val CrewMemberModel.Status.text get() = when (this) {
    CrewMemberModel.Status.ACTIVE -> R.string.crew_search_member_active
    CrewMemberModel.Status.INACTIVE -> R.string.crew_search_member_not_active
    CrewMemberModel.Status.RETIRED -> R.string.crew_search_member_retired
    CrewMemberModel.Status.UNKNOWN -> R.string.crew_search_member_unknown
}

@Composable private fun ActiveIndicator(state: CrewMemberModel.Status) {
    Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = state.color)) {
        Box(Modifier.padding(8.dp)) {
            Text(
                stringResource(state.text).toUpperCase(Locale.current),
                style = MaterialTheme.typography.labelLarge
            )
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

@Preview
@Composable
private fun Prev() {
    SpaceXTheme {
        CrewSearchSuccessLayout(listOf(
            CrewMemberModel(
                "Robert Behnken",
                CrewMemberModel.Status.ACTIVE,
                "https://imgur.com/0smMgMH.png",
                "https://en.wikipedia.org/wiki/Robert_L._Behnken"
            ),
            CrewMemberModel(
                "Douglas Hurley",
                CrewMemberModel.Status.INACTIVE,
                "https://i.imgur.com/ooaayWf.png",
                "https://en.wikipedia.org/wiki/Douglas_G._Hurley"
            ),
            CrewMemberModel(
                "Shannon Walker",
                CrewMemberModel.Status.RETIRED,
                "https://i.imgur.com/ooaayWf.png",
                "\"https://en.wikipedia.org/wiki/Shannon_Walker"
            ),
            CrewMemberModel(
                "Soichi Noguchi",
                CrewMemberModel.Status.UNKNOWN,
                "https://imgur.com/7B1jxl8.png",
                "https://en.wikipedia.org/wiki/Soichi_Noguchi"
            )

        ), {})
    }
}
