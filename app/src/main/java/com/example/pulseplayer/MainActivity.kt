package com.example.pulseplayer

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.pulseplayer.ui.theme.PulsePlayerTheme

data class Song(val title: String, val artist: String, val url: String)
data class Album(val name: String, val songs: List<Song>)

enum class Tab { Home, Search, Library }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulsePlayerTheme {
                PulsePlayerApp()
            }
        }
    }
}

@Composable
fun PulsePlayerApp() {
    val controller = rememberMediaController()
    val albums = remember { sampleAlbums() }
    val allSongs = remember(albums) { albums.flatMap { it.songs } }

    var selectedTab by remember { mutableStateOf(Tab.Home) }
    var showAlbums by remember { mutableStateOf(false) }
    var nowPlaying by remember { mutableStateOf(allSongs.first()) }

    val violet = Color(0xFF7C3AED)
    val indigo = Color(0xFF4F46E5)

    fun playSong(song: Song) {
        nowPlaying = song
        val mediaItems = allSongs.map {
            MediaItem.Builder()
                .setUri(it.url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(it.title)
                        .setArtist(it.artist)
                        .build()
                )
                .build()
        }
        controller?.setMediaItems(mediaItems)
        controller?.prepare()
        controller?.seekTo(allSongs.indexOf(song), 0)
        controller?.playWhenReady = true
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = Color(0xFF0A0A14),
        bottomBar = {
            BottomBar(selectedTab = selectedTab) {
                selectedTab = it
                showAlbums = it == Tab.Library
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF080815), Color(0xFF090E22), Color(0xFF070710))
                    )
                )
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (showAlbums) {
                AlbumsScreen(albums = albums, onSongSelected = ::playSong)
            } else {
                HomeScreen(
                    nowPlaying = nowPlaying,
                    onMusicClick = { showAlbums = true; selectedTab = Tab.Library },
                    onAlbumsClick = { showAlbums = true; selectedTab = Tab.Library },
                    onPlayClick = { playSong(nowPlaying) },
                    onPrevClick = { controller?.seekToPreviousMediaItem() },
                    onNextClick = { controller?.seekToNextMediaItem() },
                    violet = violet,
                    indigo = indigo
                )
            }
        }
    }

    LaunchedEffect(controller) {
        controller?.let {
            if (it.mediaItemCount == 0) {
                playSong(nowPlaying)
                it.pause()
            }
        }
    }
}

@Composable
private fun rememberMediaController(): MediaController? {
    val context = LocalContext.current
    val sessionToken = remember {
        SessionToken(context, ComponentName(context, PlaybackService::class.java))
    }
    var controller by remember { mutableStateOf<MediaController?>(null) }

    DisposableEffect(sessionToken) {
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener(
            { controller = future.get() },
            ContextCompat.getMainExecutor(context)
        )

        onDispose {
            if (future.isDone) {
                controller?.release()
            } else {
                future.cancel(true)
            }
            controller = null
        }
    }

    return controller
}

@Composable
private fun HomeScreen(
    nowPlaying: Song,
    onMusicClick: () -> Unit,
    onAlbumsClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    violet: Color,
    indigo: Color
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(violet, indigo))),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.MusicNote, null, tint = Color.White) }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Pulse Player", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text("Reproductor Local", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color(0x22FFFFFF))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Settings, null, tint = Color(0xFFCBD5E1)) }
            }
        }
        item {
            Card(
                onClick = onMusicClick,
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.height(220.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Color(0xFF5B21B6), Color(0xFF1E3A8A), Color(0xFF172554))))
                        .padding(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x22FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(38.dp)) }
                        Spacer(Modifier.height(10.dp))
                        Text("Música", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
                        Text("Explorar biblioteca", color = Color(0xFFCBD5E1), fontSize = 18.sp)
                    }
                }
            }
        }
        item {
            Text("BIBLIOTECA", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        item {
            LibraryGrid(onAlbumsClick = onAlbumsClick)
        }
        item {
            MiniPlayer(
                nowPlaying = nowPlaying,
                onPlayClick = onPlayClick,
                onPrevClick = onPrevClick,
                onNextClick = onNextClick,
                violet = violet,
                indigo = indigo
            )
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun LibraryGrid(onAlbumsClick: () -> Unit) {
    val items = listOf(
        Triple("Álbumes", "24 álbumes", Icons.Default.GridView),
        Triple("Listas", "8 listas", Icons.Default.Album),
        Triple("Historial", "Recientes", Icons.Outlined.History),
        Triple("Favoritos", "156 canciones", Icons.Outlined.FavoriteBorder)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(300.dp)
    ) {
        items(items) { item ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101A34)),
                modifier = Modifier
                    .height(140.dp)
                    .clickable { if (item.first == "Álbumes") onAlbumsClick() }
            ) {
                Column(Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.third, null, tint = Color(0xFF8B5CF6))
                    }
                    Spacer(Modifier.weight(1f))
                    Text(item.first, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(item.second, color = Color(0xFF64748B), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun MiniPlayer(
    nowPlaying: Song,
    onPlayClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    violet: Color,
    indigo: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF121A2E))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(violet, indigo))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, null, tint = Color.White)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(nowPlaying.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(nowPlaying.artist, color = Color(0xFF94A3B8), fontSize = 13.sp)
        }
        IconButton(onClick = onPrevClick) { Icon(Icons.Outlined.SkipPrevious, null, tint = Color(0xFFCBD5E1)) }
        IconButton(
            onClick = onPlayClick,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(violet, indigo)))
        ) { Icon(Icons.Default.PlayArrow, null, tint = Color.White) }
        IconButton(onClick = onNextClick) { Icon(Icons.Outlined.SkipNext, null, tint = Color(0xFFCBD5E1)) }
    }
}

@Composable
private fun AlbumsScreen(albums: List<Album>, onSongSelected: (Song) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(14.dp))
        Text("Álbumes", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Toca una canción para reproducir y mantener control en segundo plano.", color = Color(0xFF94A3B8))
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(albums.size) { index ->
                val album = albums[index]
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101A34))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(album.name, color = Color.White, fontWeight = FontWeight.Bold)
                        album.songs.forEach { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSongSelected(song) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.MusicNote, null, tint = Color(0xFF8B5CF6))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(song.title, color = Color.White)
                                    Text(song.artist, color = Color(0xFF94A3B8), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun BottomBar(selectedTab: Tab, onTabSelected: (Tab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF070B18))
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BottomBarItem("Inicio", Icons.Default.Home, selectedTab == Tab.Home) { onTabSelected(Tab.Home) }
        BottomBarItem("Buscar", Icons.Default.Search, selectedTab == Tab.Search) { onTabSelected(Tab.Search) }
        BottomBarItem("Biblioteca", Icons.Default.GridView, selectedTab == Tab.Library) { onTabSelected(Tab.Library) }
    }
}

@Composable
private fun BottomBarItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val tint = if (selected) Color(0xFF8B5CF6) else Color(0xFF64748B)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Icon(icon, null, tint = tint)
        Text(label, color = tint, fontSize = 12.sp)
    }
}

private fun sampleAlbums() = listOf(
    Album(
        "The Classics",
        listOf(
            Song("Midnight Dreams", "The Classics", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
            Song("Neon Skies", "The Classics", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3")
        )
    ),
    Album(
        "Night Drive",
        listOf(
            Song("Moonlight Run", "Night Drive", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"),
            Song("Echoes", "Night Drive", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3")
        )
    )
)

@Preview(showBackground = true)
@Composable
private fun PulsePlayerPreview() {
    PulsePlayerTheme {
        PulsePlayerApp()
    }
}
