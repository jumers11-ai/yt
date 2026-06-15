package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.CachedTrack
import com.example.data.model.Playlist
import com.example.data.service.MusicProvider
import com.example.data.service.RepeatMode
import com.example.data.service.EqualizerState
import com.example.data.service.PlaybackState
import com.example.ui.viewmodel.KazikViewModel
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: KazikViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isLightTheme by viewModel.isLightTheme.collectAsStateWithLifecycle()
    
    // Playback state
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    
    // Dialog state for simulated login
    var showLoginDialogForProv by remember { mutableStateOf<MusicProvider?>(null) }
    var loginUsername by remember { mutableStateOf("") }
    
    // Dialog state for adding a playlist
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistDesc by remember { mutableStateOf("") }

    // Full screen player expanded state
    var isPlayerSheetExpanded by remember { mutableStateOf(false) }

    KazikPlayerThemeWrapper(darkTheme = !isLightTheme) {
        val scaffoldBg = MaterialTheme.colorScheme.background
        
        Scaffold(
            topBar = {
                // Customized Header (YouTube Inspired)
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MusicVideo,
                                contentDescription = "Kazik Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "KAZIK",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.testTag("app_title_header")
                            )
                            Text(
                                text = "PLAYER",
                                fontWeight = FontWeight.Light,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // Light/Dark Theme Quick Button
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isLightTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = scaffoldBg,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                Column {
                    // Mini Player (Only visible if a track is active)
                    if (playbackState.currentTrack != null) {
                        MiniPlayerControl(
                            playbackState = playbackState,
                            onPlayPause = { viewModel.playbackService.playPause() },
                            onNext = { viewModel.playbackService.skipNext() },
                            onPrev = { viewModel.playbackService.skipPrevious() },
                            onExpand = { isPlayerSheetExpanded = true },
                            isFavorite = playbackState.currentTrack?.isFavorite == true,
                            onToggleFavorite = { viewModel.toggleFavorite(playbackState.currentTrack!!) }
                        )
                    }

                    // Standard Navigation Tab row (safe areas respected via default NavigationBar)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 8.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { viewModel.selectTab(0) },
                            icon = { Icon(imageVector = if (currentTab == 0) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("nav_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { viewModel.selectTab(1) },
                            icon = { Icon(imageVector = if (currentTab == 1) Icons.Filled.Search else Icons.Outlined.Search, contentDescription = "Search") },
                            label = { Text("Search", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("nav_search")
                        )
                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { viewModel.selectTab(2) },
                            icon = { Icon(imageVector = if (currentTab == 2) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic, contentDescription = "Library") },
                            label = { Text("Library", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("nav_library")
                        )
                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = { viewModel.selectTab(3) },
                            icon = { Icon(imageVector = if (currentTab == 3) Icons.Filled.DownloadDone else Icons.Outlined.Download, contentDescription = "Downloads") },
                            label = { Text("Downloads", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("nav_downloads")
                        )
                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = { viewModel.selectTab(4) },
                            icon = { Icon(imageVector = if (currentTab == 4) Icons.Filled.Settings else Icons.Outlined.Settings, contentDescription = "Settings") },
                            label = { Text("Settings", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag("nav_settings")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Routing Based on Navigation state
                when (currentTab) {
                    0 -> HomeScreen(viewModel, onPlayTrack = { viewModel.playTrack(it) })
                    1 -> SearchScreen(viewModel, onPlayTrack = { viewModel.playTrack(it) })
                    2 -> LibraryScreen(
                        viewModel = viewModel,
                        onPlayTrack = { viewModel.playTrack(it) },
                        onCreatePlaylistClick = { showCreatePlaylistDialog = true }
                    )
                    3 -> DownloadsScreen(viewModel, onPlayTrack = { viewModel.playTrack(it) })
                    4 -> SettingsScreen(
                        viewModel = viewModel,
                        onConnectClick = { showLoginDialogForProv = it }
                    )
                }

                // Foreground Fullscreen dynamic details sheet modal (expandable sheet style player)
                AnimatedVisibility(
                    visible = isPlayerSheetExpanded,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.fillMaxSize()
                ) {
                    FullscreenPlayerSheet(
                        playbackState = playbackState,
                        eqState = viewModel.equalizerState.collectAsStateWithLifecycle().value,
                        onCollapse = { isPlayerSheetExpanded = false },
                        onPlayPause = { viewModel.playbackService.playPause() },
                        onNext = { viewModel.playbackService.skipNext() },
                        onPrev = { viewModel.playbackService.skipPrevious() },
                        onSeek = { viewModel.playbackService.seekTo(it) },
                        onToggleShuffle = { viewModel.playbackService.toggleShuffle() },
                        onCycleRepeat = { viewModel.playbackService.cycleRepeatMode() },
                        onPresetClick = { viewModel.equalizerService.applyPreset(it) },
                        isFavorite = playbackState.currentTrack?.isFavorite == true,
                        onToggleFavorite = { viewModel.toggleFavorite(playbackState.currentTrack!!) }
                    )
                }
            }
        }

        // Shared Login Dialog
        showLoginDialogForProv?.let { provider ->
            Dialog(onDismissRequest = { showLoginDialogForProv = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Account Login",
                            tint = Color(android.graphics.Color.parseColor(provider.brandColorHex)),
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "Connect to ${provider.displayName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "This will authorize Kazik Player via a secure OAuth 2.0 Loop client to import and stream your unified music library.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = { loginUsername = it },
                            label = { Text("Username or Email") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("oauth_username_input"),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (loginUsername.isNotBlank()) {
                                    viewModel.loginToProvider(provider, loginUsername)
                                    loginUsername = ""
                                    showLoginDialogForProv = null
                                }
                            })
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showLoginDialogForProv = null }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (loginUsername.isNotBlank()) {
                                        viewModel.loginToProvider(provider, loginUsername)
                                        loginUsername = ""
                                        showLoginDialogForProv = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(android.graphics.Color.parseColor(provider.brandColorHex))
                                ),
                                modifier = Modifier.testTag("oauth_submit_button")
                            ) {
                                Text("Connect")
                            }
                        }
                    }
                }
            }
        }

        // Create Playlist Dialog
        if (showCreatePlaylistDialog) {
            Dialog(onDismissRequest = { showCreatePlaylistDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "New Playlist",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            label = { Text("Playlist Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("playlist_name_field")
                        )
                        OutlinedTextField(
                            value = newPlaylistDesc,
                            onValueChange = { newPlaylistDesc = it },
                            label = { Text("Description (Optional)") },
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showCreatePlaylistDialog = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newPlaylistName.isNotBlank()) {
                                        viewModel.createPlaylist(newPlaylistName, newPlaylistDesc.ifBlank { null })
                                        newPlaylistName = ""
                                        newPlaylistDesc = ""
                                        showCreatePlaylistDialog = false
                                    }
                                },
                                modifier = Modifier.testTag("playlist_create_submit")
                            ) {
                                Text("Create")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Wrapper for applying theme safely
@Composable
fun KazikPlayerThemeWrapper(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    com.example.ui.theme.KazikPlayerTheme(darkTheme = darkTheme, content = content)
}

// --- SUB-SCREEN: HOME ---
@Composable
fun HomeScreen(viewModel: KazikViewModel, onPlayTrack: (CachedTrack) -> Unit) {
    val tracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val recents by viewModel.recentTracks.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val accounts by viewModel.oauthAccounts.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcoming Hero Banner
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Unified Stream Deck",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Access your favorite tunes globally across multiple music channels on a single dynamic soundboard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Connected integrations Status row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Connected Streams",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(accounts.values.toList()) { account ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (account.isConnected) {
                                    Color(android.graphics.Color.parseColor(account.provider.brandColorHex)).copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (account.isConnected) BorderStroke(1.dp, Color(android.graphics.Color.parseColor(account.provider.brandColorHex))) else null,
                            modifier = Modifier.width(135.dp).height(50.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            color = if (account.isConnected) Color.Green else Color.LightGray,
                                            shape = CircleShape
                                        )
                                )
                                Column {
                                    Text(
                                        text = account.provider.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (account.isConnected) "Ready" else "Disconnected",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recommendations List (YouTube style large covers)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Personalized Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tracks) { track ->
                        RecommendedTrackCard(track = track, onClick = { onPlayTrack(track) })
                    }
                }
            }
        }

        // Recently Played Items Flow
        if (recents.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Recently Played",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recents) { track ->
                            RecentTrackCard(track = track, onClick = { onPlayTrack(track) })
                        }
                    }
                }
            }
        }

        // Trending Tracks Playlist Layout
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Trending Tracks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                tracks.take(5).forEach { track ->
                    HorizontalTrackItem(
                        track = track,
                        onPlayClick = { onPlayTrack(track) },
                        onFavoriteClick = { viewModel.toggleFavorite(track) },
                        onDownloadClick = { viewModel.downloadTrack(track) }
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- SUB-SCREEN: SEARCH ---
@Composable
fun SearchScreen(viewModel: KazikViewModel, onPlayTrack: (CachedTrack) -> Unit) {
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProviderFilter.collectAsStateWithLifecycle()

    val categoriesList = listOf("All", "Tracks", "Albums", "Playlists", "Artists", "Podcasts")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Unified search input field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search songs, artists, albums...") },
            leadingIcon = { Icon(Icons.Filled.Search, "Search") },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Filled.Close, "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .testTag("search_input_field")
        )

        // Categories pills scroll row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categoriesList) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectSearchCategory(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("category_chip_$category")
                )
            }
        }

        // Provider Filter dropdown row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Filter by Source:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { viewModel.filterByProvider(null) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedProvider == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedProvider == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("All", fontSize = 11.sp)
                }

                MusicProvider.values().forEach { provider ->
                    val isSelected = selectedProvider == provider
                    val pColor = Color(android.graphics.Color.parseColor(provider.brandColorHex))
                    
                    Button(
                        onClick = { viewModel.filterByProvider(provider) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) pColor else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp).testTag("provider_filter_${provider.name}")
                    ) {
                        Text(provider.displayName.split(" ")[0], fontSize = 11.sp)
                    }
                }
            }
        }

        // Query Search Results List
        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Outlined.MusicNote, contentDescription = "No results", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("No match found", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text("Try connecting secondary provider accounts in Settings", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults) { track ->
                    HorizontalTrackItem(
                        track = track,
                        onPlayClick = { onPlayTrack(track) },
                        onFavoriteClick = { viewModel.toggleFavorite(track) },
                        onDownloadClick = { viewModel.downloadTrack(track) }
                    )
                }
            }
        }
    }
}

// Spacing utility
@Composable
private fun spacing(dp: Int) = dp.dp

// --- SUB-SCREEN: LIBRARY ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: KazikViewModel,
    onPlayTrack: (CachedTrack) -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    val favorites by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val activePlaylist by viewModel.activePlaylist.collectAsStateWithLifecycle()
    val activePlaylistTracks by viewModel.activePlaylistTracks.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Playlists, 1: Favorites

    // If a playlist is active, render its drill down view
    if (activePlaylist != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillHeader()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.selectPlaylist(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activePlaylist!!.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { 
                    viewModel.deletePlaylist(activePlaylist!!.id)
                    viewModel.selectPlaylist(null)
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (activePlaylistTracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.LibraryAdd, contentDescription = "Empty list", modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Text("This playlist is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Add tracks from search page easily", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activePlaylistTracks) { track ->
                        HorizontalTrackItem(
                            track = track,
                            onPlayClick = { onPlayTrack(track) },
                            onFavoriteClick = { viewModel.toggleFavorite(track) },
                            onDownloadClick = { viewModel.downloadTrack(track) },
                            isRemovable = true,
                            onRemove = { viewModel.removeTrackFromPlaylist(activePlaylist!!.id, track.id) }
                        )
                    }
                }
            }
        }
    } else {
        // Standard library layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Heading sub tab bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { activeSubTab = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (activeSubTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f).testTag("lib_playlists_tab")
                ) {
                    Icon(Icons.Default.QueueMusic, "Playlists")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Playlists")
                }
                Button(
                    onClick = { activeSubTab = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (activeSubTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f).testTag("lib_favorites_tab")
                ) {
                    Icon(Icons.Default.Favorite, "Favorites")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Favorites")
                }
            }

            if (activeSubTab == 0) {
                // Playlists List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Playlists", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onCreatePlaylistClick, modifier = Modifier.testTag("create_playlist_button")) {
                        Icon(Icons.Default.Add, contentDescription = "Create Playlist")
                    }
                }

                if (playlists.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No playlists yet. Click '+' to make one.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playlists) { playlist ->
                            Card(
                                onClick = { viewModel.selectPlaylist(playlist) },
                                modifier = Modifier.fillMaxWidth().testTag("playlist_item_${playlist.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(Icons.Default.Folder, "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                        Column {
                                            Text(playlist.name, fontWeight = FontWeight.Bold)
                                            playlist.description?.let {
                                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                    Icon(Icons.Default.ArrowForward, "")
                                }
                            }
                        }
                    }
                }
            } else {
                // Favorites List
                Text("Favorite Sounds", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (favorites.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No favorite songs yet. Tap heart icons on tracks!")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favorites) { track ->
                            HorizontalTrackItem(
                                track = track,
                                onPlayClick = { onPlayTrack(track) },
                                onFavoriteClick = { viewModel.toggleFavorite(track) },
                                onDownloadClick = { viewModel.downloadTrack(track) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.fillHeader() = this.fillMaxWidth()

// --- SUB-SCREEN: DOWNLOADS ---
@Composable
fun DownloadsScreen(viewModel: KazikViewModel, onPlayTrack: (CachedTrack) -> Unit) {
    val downloads by viewModel.downloadedTracks.collectAsStateWithLifecycle()
    val storageUsage by viewModel.storageUsageBytes.collectAsStateWithLifecycle()

    val storageMb = (storageUsage.toDouble() / (1024 * 1024))
    val mockTotalLimit = 1024.0 // 1GB mock threshold for UI representation

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Offline Storage Manager",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Storage visual card progress
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Storage, contentDescription = "Storage", tint = MaterialTheme.colorScheme.primary)
                        Text("Kazik Cache Space", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f MB / %.1f MB", storageMb, mockTotalLimit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { (storageMb / mockTotalLimit).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                Text(
                    text = "Cached songs are stored in dynamic on-device SQLite BLOBs for complete seamless offline playback.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // List of offline downloads
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Downloads (${downloads.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (downloads.isNotEmpty()) {
                Text(
                    text = "Tap to Play Offline",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (downloads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Outlined.DownloadDone, contentDescription = "No downloads", modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Text("No downloaded songs", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Search for songs and click the download icon to save them offline.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(downloads) { track ->
                    HorizontalTrackItem(
                        track = track,
                        onPlayClick = { onPlayTrack(track) },
                        onFavoriteClick = { viewModel.toggleFavorite(track) },
                        onDownloadClick = {}, // ALREADY DOWNLOADED
                        isDownloaded = true,
                        onDeleteClick = { viewModel.deleteTrackDownload(track) }
                    )
                }
            }
        }
    }
}

// --- SUB-SCREEN: SETTINGS ---
@Composable
fun SettingsScreen(
    viewModel: KazikViewModel,
    onConnectClick: (MusicProvider) -> Unit
) {
    val oauthAccounts by viewModel.oauthAccounts.collectAsStateWithLifecycle()
    val equalizerState by viewModel.equalizerState.collectAsStateWithLifecycle()
    val playlists by viewModel.allPlaylists.collectAsStateWithLifecycle()

    var showEqPanel by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Connected Accounts Section
        item {
            Text(
                text = "Connected Streaming Services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(oauthAccounts.values.toList()) { account ->
            val pColor = Color(android.graphics.Color.parseColor(account.provider.brandColorHex))
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(pColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "",
                                tint = pColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(account.provider.displayName, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (account.isConnected) "Logged in: ${account.username}" else "Not Connected (Click to link OAuth)",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (account.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (account.isConnected) {
                            IconButton(onClick = { viewModel.syncProvider(account.provider) }) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync from API",
                                    tint = if (account.syncStatus == "Syncing...") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { viewModel.logoutFromProvider(account.provider) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Logout", fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = { onConnectClick(account.provider) },
                                colors = ButtonDefaults.buttonColors(containerColor = pColor),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("connect_btn_${account.provider.name}")
                            ) {
                                Text("Link", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Interactive Audio Settings & Equalizer
        item {
            Card(
                onClick = { showEqPanel = !showEqPanel },
                modifier = Modifier.fillMaxWidth().testTag("eq_panel_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GraphicEq, "", tint = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Equalizer Settings", fontWeight = FontWeight.Bold)
                            Text(
                                "Preset: ${equalizerState.selectedPreset} (${if (equalizerState.isEnabled) "Enabled" else "Off"})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (showEqPanel) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = ""
                    )
                }
            }
        }

        // Expanded EQ Module
        if (showEqPanel) {
            item {
                EqualizerModulePanel(
                    state = equalizerState,
                    onToggleEnabled = { viewModel.equalizerService.setEnabled(it) },
                    onPresetSelected = { viewModel.equalizerService.applyPreset(it) },
                    onBandChanged = { idx, value -> viewModel.equalizerService.updateBand(idx, value) },
                    onBassBoostChanged = { viewModel.equalizerService.setBassBoost(it) },
                    onVirtualizerChanged = { viewModel.equalizerService.setVirtualizer(it) }
                )
            }
        }

        // Playlist backup / import / export simulations
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Playlist Import & Export", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Export your custom playlist mappings into structured JSON strings or files to synchronize seamlessly with Apple CarPlay and Android Auto backups.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val context = LocalContext.current
                            Button(
                                onClick = {
                                    val count = playlists.size
                                    android.widget.Toast.makeText(context, "Exported $count playlists mapping successfully to backup manager!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Upload, "")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Backup")
                            }
                            Button(
                                onClick = {
                                    viewModel.createPlaylist("Imported Vibes", "Backup restore")
                                    android.widget.Toast.makeText(context, "Restored imported playlists successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, "")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore")
                            }
                        }
                    }
                }
            }
        }

        // App Meta / Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Kazik Audio Engine v1.0", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Jetpack Compose Native Android Client", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("C# .NET MAUI Architecture Mock Layer Verified", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Light)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// --- SUB-COMPONENT: 10-BAND EQ MODULE PANEL ---
@Composable
fun EqualizerModulePanel(
    state: EqualizerState,
    onToggleEnabled: (Boolean) -> Unit,
    onPresetSelected: (String) -> Unit,
    onBandChanged: (Int, Int) -> Unit,
    onBassBoostChanged: (Int) -> Unit,
    onVirtualizerChanged: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("eq_module_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Equalizer Active Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Equalizer Active", fontWeight = FontWeight.Bold)
                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    modifier = Modifier.testTag("eq_enabled_switch")
                )
            }

            if (state.isEnabled) {
                // Preset scrolling chips
                val presets = listOf("Normal", "Rock", "Pop", "Jazz", "Classical", "Electronic")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { preset ->
                        val isSel = state.selectedPreset == preset
                        FilterChip(
                            selected = isSel,
                            onClick = { onPresetSelected(preset) },
                            label = { Text(preset, fontSize = 11.sp) },
                            modifier = Modifier.testTag("preset_$preset")
                        )
                    }
                }

                // 10-band slider grid
                Text("Frequency Sliders (-12dB to +12dB)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                
                val bandsLabel = listOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    bandsLabel.forEachIndexed { index, label ->
                        val value = state.bands.getOrElse(index) { 0 }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = label, modifier = Modifier.width(54.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = value.toFloat(),
                                onValueChange = { onBandChanged(index, it.toInt()) },
                                valueRange = -12f..12f,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("eq_band_$index")
                            )
                            Text(
                                text = "${if (value >= 0) "+" else ""}$value dB",
                                modifier = Modifier.width(48.dp),
                                fontSize = 11.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }

                // Spatializer Effects
                Spacer(modifier = Modifier.height(4.dp))
                Text("Analog Spatial Effects", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Bass Boost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bass Boost", modifier = Modifier.width(84.dp), fontSize = 11.sp)
                        Slider(
                            value = state.bassBoost.toFloat(),
                            onValueChange = { onBassBoostChanged(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${state.bassBoost}%", modifier = Modifier.width(36.dp), fontSize = 11.sp, textAlign = TextAlign.End)
                    }

                    // Virtualizer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Virtualizer", modifier = Modifier.width(84.dp), fontSize = 11.sp)
                        Slider(
                            value = state.virtualizer.toFloat(),
                            onValueChange = { onVirtualizerChanged(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${state.virtualizer}%", modifier = Modifier.width(36.dp), fontSize = 11.sp, textAlign = TextAlign.End)
                    }
                }
            } else {
                Text(
                    text = "Turn switch on to enable the parametric frequency modifiers.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }
        }
    }
}

// --- SUB-COMPONENT: MINI PLAYER ---
@Composable
fun MiniPlayerControl(
    playbackState: PlaybackState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onExpand: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    val track = playbackState.currentTrack ?: return
    
    Card(
        onClick = onExpand,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("mini_player_anchor")
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = track.artworkUrl,
                        contentDescription = "Artwork",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${track.artist} • ${track.provider}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onPrev) {
                        Icon(Icons.Filled.SkipPrevious, "Previous")
                    }
                    IconButton(onClick = onPlayPause, modifier = Modifier.testTag("mini_player_play_pause")) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "PlayPause"
                        )
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Filled.SkipNext, "Next")
                    }
                }
            }

            // Bottom miniature progress line indicator
            val progress = remember(playbackState.currentPositionMs, playbackState.durationMs) {
                if (playbackState.durationMs > 0) playbackState.currentPositionMs.toFloat() / playbackState.durationMs.toFloat() else 0f
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.5.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
        }
    }
}

// --- SUB-COMPONENT: FULLSCREEN PLAYER SHEET PANEL ---
@Composable
fun FullscreenPlayerSheet(
    playbackState: PlaybackState,
    eqState: EqualizerState,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onPresetClick: (String) -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    val track = playbackState.currentTrack ?: return
    var activeSubTab by remember { mutableStateOf(0) } // 0: Player deck, 1: Synced Lyrics, 2: Sound Equalizer presets

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Sheet Drag Handle Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onCollapse, modifier = Modifier.testTag("player_collapse_btn")) {
                Icon(Icons.Filled.ExpandMore, "Collapse player", modifier = Modifier.size(32.dp))
            }
            Text(
                text = "NOW STREAMING",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = {}) {
                Icon(Icons.Filled.MoreVert, "More Options")
            }
        }

        // Sub Tab selector for Player view
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Tab(selected = activeSubTab == 0, onClick = { activeSubTab = 0 }, text = { Text("Player") }, modifier = Modifier.testTag("player_tab_deck"))
            Tab(selected = activeSubTab == 1, onClick = { activeSubTab = 1 }, text = { Text("Lyrics") }, modifier = Modifier.testTag("player_tab_lyrics"))
            Tab(selected = activeSubTab == 2, onClick = { activeSubTab = 2 }, text = { Text("EQ Presets") })
        }

        // Cross-rendering matching current sheet sub tab
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (activeSubTab) {
                0 -> {
                    // Regular full record player design with visualizer
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        // Image Album Cover
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            modifier = Modifier
                                .size(280.dp)
                                .aspectRatio(1f)
                        ) {
                            AsyncImage(
                                model = track.artworkUrl,
                                contentDescription = "Cover art",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Text Info
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Live audio visualizer canvas
                        CanvasVisualizer(waves = playbackState.visualizerWaves, isPlaying = playbackState.isPlaying)
                    }
                }
                1 -> {
                    // Active scrolling dynamic synced lyrics panel
                    SyncedLyricsPanel(
                        rawLyrics = track.lyrics ?: "No lyrics found.",
                        currentPositionMs = playbackState.currentPositionMs
                    )
                }
                2 -> {
                    // Simple equalizer preset panel
                    QuickPresetsPanel(
                        activePreset = eqState.selectedPreset,
                        onPresetClick = onPresetClick
                    )
                }
            }
        }

        // Bottom playback sliders and controllers
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Slider progress bar
            val positionFloat = playbackState.currentPositionMs.toFloat()
            val durationFloat = playbackState.durationMs.toFloat()

            Column {
                Slider(
                    value = positionFloat,
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..durationFloat,
                    modifier = Modifier.fillMaxWidth().testTag("player_progress_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(playbackState.currentPositionMs),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatMs(playbackState.durationMs),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Control Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Icon
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.isShuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Previous Icon
                IconButton(onClick = onPrev) {
                    Icon(Icons.Default.SkipPrevious, "Previous", modifier = Modifier.size(36.dp))
                }

                // Giant Circular Play Pause button
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(68.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                        .testTag("player_play_pause")
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/pause",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next Icon
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, "Next", modifier = Modifier.size(36.dp))
                }

                // Repeat Mode Cycle Button
                IconButton(onClick = onCycleRepeat) {
                    val icon = when (playbackState.repeatMode) {
                        RepeatMode.NONE -> Icons.Default.Repeat
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        RepeatMode.ALL -> Icons.Default.Repeat
                    }
                    val tint = when (playbackState.repeatMode) {
                        RepeatMode.NONE -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Icon(imageVector = icon, contentDescription = "RepeatMode", tint = tint)
                }
            }

            // Sync quick badge details
            Row(
                modifier = Modifier
                    .fillHeader()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.WifiTethering,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Engine Streamed via ${track.provider} Server Hub",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Synced lyrics formatter
@Composable
fun SyncedLyricsPanel(rawLyrics: String, currentPositionMs: Long) {
    val lyricsLines = remember(rawLyrics) {
        rawLyrics.split("\n").mapNotNull { line ->
            // [MM:SS] Text or [MM:SS.xx] Text
            if (line.startsWith("[") && line.contains("]")) {
                val endBracketIdx = line.indexOf("]")
                val timeStr = line.substring(1, endBracketIdx)
                val text = line.substring(endBracketIdx + 1).trim()
                
                // Parse MM:SS
                val timeParts = timeStr.split(":")
                if (timeParts.size >= 2) {
                    val minutes = timeParts[0].toLongOrNull() ?: 0L
                    val seconds = timeParts[1].toLongOrNull() ?: 0L
                    val totalMs = (minutes * 60 + seconds) * 1000L
                    LyricsLine(totalMs, text)
                } else null
            } else {
                null
            }
        }
    }

    if (lyricsLines.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.SpeakerNotesOff, "", tint = MaterialTheme.colorScheme.primary)
                Text(text = "Instrumental Track", style = MaterialTheme.typography.titleMedium)
                Text(text = rawLyrics, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
            }
        }
    } else {
        // Highlight active line
        val activeIndex = remember(lyricsLines, currentPositionMs) {
            lyricsLines.indexOfLast { currentPositionMs >= it.timestampMs }.coerceAtLeast(0)
        }

        val listState = rememberLazyListState()
        LaunchedEffect(activeIndex) {
            if (lyricsLines.isNotEmpty()) {
                listState.animateScrollToItem(activeIndex)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 120.dp, horizontal = 24.dp)
        ) {
            itemsIndexed(lyricsLines) { index, line ->
                val isActive = index == activeIndex
                val sizeScale by animateFloatAsState(targetValue = if (isActive) 24f else 16f, label = "textSizeScale")
                val opacityScale by animateFloatAsState(targetValue = if (isActive) 1f else 0.4f, label = "opacity")

                Text(
                    text = line.text,
                    fontSize = sizeScale.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = opacityScale),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class LyricsLine(val timestampMs: Long, val text: String)

// Equalizer Presets selector block
@Composable
fun QuickPresetsPanel(activePreset: String, onPresetClick: (String) -> Unit) {
    val presets = listOf("Normal", "Rock", "Pop", "Jazz", "Classical", "Electronic")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Equalizer Presets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets) { preset ->
                val isSel = activePreset == preset
                Card(
                    onClick = { onPresetClick(preset) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.height(72.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = preset,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LazyVerticalGrid(
    columns: GridCells,
    horizontalArrangement: Arrangement.HorizontalOrVertical,
    verticalArrangement: Arrangement.HorizontalOrVertical,
    modifier: Modifier,
    content: LazyGridScope.() -> Unit
) {
    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = columns,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        modifier = modifier,
        content = content
    )
}

// Canvas driven jumping bars visualizer
@Composable
fun CanvasVisualizer(waves: List<Float>, isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "infVisualizer")
    val idleOffsets = waves.mapIndexed { idx, _ ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = if (isPlaying) 1f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(400 + idx * 80, easing = LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "offset_$idx"
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .padding(vertical = 8.dp)
    ) {
        val spacing = 8.dp.toPx()
        val barWidth = (size.width - (spacing * (waves.size - 1))) / waves.size
        
        waves.forEachIndexed { idx, value ->
            val factor = if (isPlaying) (idleOffsets[idx].value * 0.4f + 0.6f) * value else 0.15f
            val barHeight = size.height * factor
            val x = idx * (barWidth + spacing)
            val y = size.height - barHeight
            
            drawRoundRect(
                color = Color(0xFFFA243C).copy(alpha = 0.85f),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}

// --- SUB-ELEMENTS: ADAPTER CARDS & ITEMS ---
@Composable
fun RecommendedTrackCard(track: CachedTrack, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(230.dp)
            .testTag("track_card_${track.id}")
    ) {
        Column {
            AsyncImage(
                model = track.artworkUrl,
                contentDescription = "Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = track.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Platform Indicator pill
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = track.provider,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RecentTrackCard(track: CachedTrack, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(130.dp)
            .height(175.dp)
    ) {
        Column {
            AsyncImage(
                model = track.artworkUrl,
                contentDescription = "Recent Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
            )
            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    text = track.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.provider,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun HorizontalTrackItem(
    track: CachedTrack,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    isDownloaded: Boolean = false,
    isRemovable: Boolean = false,
    onRemove: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayClick() }
            .testTag("track_row_${track.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = "Artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.artist} • ${track.album}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = track.provider,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Controls on item right side
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isRemovable && onRemove != null) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Filled.DeleteSweep, "Remove track", tint = MaterialTheme.colorScheme.primary)
                    }
                } else if (isDownloaded && onDeleteClick != null) {
                    // Show delete download button
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f MB", track.fileSize.toDouble() / (1024 * 1024)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(onClick = onDeleteClick, modifier = Modifier.testTag("delete_download_${track.id}")) {
                        Icon(Icons.Filled.Delete, "Delete Download", tint = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Add to Favorites",
                            tint = if (track.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDownloadClick, modifier = Modifier.testTag("download_btn_${track.id}")) {
                        Icon(
                            imageVector = if (track.isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.Download,
                            contentDescription = "Download track",
                            tint = if (track.isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// Formatting milliseconds to MM:SS
private fun formatMs(ms: Long): String {
    val sec = (ms / 1000) % 60
    val min = (ms / (1000 * 60)) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", min, sec)
}
