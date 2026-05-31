package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.DirectMessageEntity
import com.example.data.database.FavoriteEntity
import com.example.data.database.WalletTransactionEntity
import com.example.data.database.WatchHistoryEntity
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ABKViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- REUSABLE GLASSMORPHIC CARD ---
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = GlassAlphaCard,
    strokeColor: Color = GlassAlphaStroke,
    cornerRadius: Float = 16f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(backgroundColor)
            .border(1.dp, strokeColor, RoundedCornerShape(cornerRadius.dp))
            .drawBehind {
                // Subtle glass gloss reflection
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(GlassAlphaReflect, Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    size = size
                )
            },
        content = content
    )
}

// --- MASTER NAVIGATION SCREEN MANAGER ---
@Composable
fun MainAppContainer(viewModel: ABKViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isRtl = currentLang.isRtl
    val direction = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    val currentScreen by viewModel.currentScreen.collectAsState()
    val activePlayContent by viewModel.activePlayContent.collectAsState()
    val activeStory by viewModel.activeStory.collectAsState()

    // Enforce RTL or LTR dynamically based on translation selection
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkVibeBg)
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (activePlayContent == null && activeStory == null) {
                        BottomGlassBar(viewModel = viewModel, currentScreen = currentScreen)
                    }
                },
                contentWindowInsets = WindowInsets.safeDrawing
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // SCREEN SWITCHER
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                        },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            ABKViewModel.Screen.HOME -> HomeScreen(viewModel)
                            ABKViewModel.Screen.CONTENT_DETAIL -> ContentDetailScreen(viewModel)
                            ABKViewModel.Screen.CREATORS -> CommunityAndStoriesScreen(viewModel)
                            ABKViewModel.Screen.CREATOR_DASHBOARD -> CreatorDashboardScreen(viewModel)
                            ABKViewModel.Screen.CHAT -> DirectChatScreen(viewModel)
                            ABKViewModel.Screen.WALLET -> DigitalWalletScreen(viewModel)
                            ABKViewModel.Screen.VERIFICATION -> VerificationScreen(viewModel)
                            ABKViewModel.Screen.ADMIN -> AdminDashboardScreen(viewModel)
                            ABKViewModel.Screen.SEARCH -> SearchScreen(viewModel)
                        }
                    }

                    // LAYER 2: INTERACTIVE STORIES FULLSCREEN POPUP
                    if (activeStory != null) {
                        FullscreenStoryOverlay(viewModel = viewModel, story = activeStory!!)
                    }

                    // LAYER 3: CINEMATIC PLAYBACK SIMULATOR
                    if (activePlayContent != null) {
                        CinematicPlayerOverlay(viewModel = viewModel, content = activePlayContent!!)
                    }
                }
            }
        }
    }
}

// --- GLASS BOTTOM BAR ---
@Composable
fun BottomGlassBar(viewModel: ABKViewModel, currentScreen: ABKViewModel.Screen) {
    val currentLang by viewModel.currentLanguage.collectAsState()

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .height(68.dp),
        backgroundColor = Color(0xD90A0A0C), // luxury dark opaque-glass
        strokeColor = GlassAlphaStroke,
        cornerRadius = 24f
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = Localization.get(currentLang.code, "home"),
                isSelected = currentScreen == ABKViewModel.Screen.HOME,
                modifier = Modifier.testTag("nav_home_btn"),
                onClick = { viewModel.navigateTo(ABKViewModel.Screen.HOME) }
            )
            BottomNavItem(
                icon = Icons.Default.Search,
                label = Localization.get(currentLang.code, "search"),
                isSelected = currentScreen == ABKViewModel.Screen.SEARCH,
                modifier = Modifier.testTag("nav_search_btn"),
                onClick = { viewModel.navigateTo(ABKViewModel.Screen.SEARCH) }
            )
            BottomNavItem(
                icon = Icons.Default.Groups,
                label = Localization.get(currentLang.code, "creators"),
                isSelected = currentScreen == ABKViewModel.Screen.CREATORS,
                modifier = Modifier.testTag("nav_creators_btn"),
                onClick = { viewModel.navigateTo(ABKViewModel.Screen.CREATORS) }
            )
            BottomNavItem(
                icon = Icons.Default.AccountBalanceWallet,
                label = Localization.get(currentLang.code, "wallet"),
                isSelected = currentScreen == ABKViewModel.Screen.WALLET,
                modifier = Modifier.testTag("nav_wallet_btn"),
                onClick = { viewModel.navigateTo(ABKViewModel.Screen.WALLET) }
            )
            BottomNavItem(
                icon = Icons.Default.Settings,
                label = Localization.get(currentLang.code, "settings"),
                isSelected = currentScreen == ABKViewModel.Screen.VERIFICATION,
                modifier = Modifier.testTag("nav_settings_btn"),
                onClick = { viewModel.navigateTo(ABKViewModel.Screen.VERIFICATION) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val barColor = if (isSelected) BrightPremiumRed else DarkGreyText

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = barColor,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = barColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


// --- HOME SCREEN DISPLAY LAYOUT ---
@Composable
fun HomeScreen(viewModel: ABKViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val moviesList = remember { viewModel.getallMainmoviesAndseries().filter { !it.isSeries } }
    val seriesList = remember { viewModel.getallMainmoviesAndseries().filter { it.isSeries } }
    val storiesList = remember { viewModel.getallStories() }
    val continueWatching by viewModel.continueWatchingList.collectAsState()

    var showLangSelector by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // TOP LOGO AND BRANDING SLATE
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LOGO WITH NEON GLOW REFLECTION
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(BrightPremiumRed, DeepBurgundy)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            color = SmoothWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = Localization.get(currentLang.code, "app_name"),
                        color = SmoothWhite,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // LANGUAGE CONTROLLER TRIDENTS
                IconButton(
                    onClick = { showLangSelector = !showLangSelector },
                    modifier = Modifier
                        .background(GlassAlphaCard, CircleShape)
                        .border(1.dp, GlassAlphaStroke, CircleShape)
                        .testTag("lang_toggle_badge")
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Language",
                        tint = BrightPremiumRed
                    )
                }
            }
        }

        // DYNAMICAL LANG SELECTOR DROPDOWN SHEET
        if (showLangSelector) {
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    cornerRadius = 20f
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = Localization.get(currentLang.code, "settings"),
                            color = BrightPremiumRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(viewModel.languages) { lang ->
                                Button(
                                    onClick = {
                                        viewModel.selectLanguage(lang.code)
                                        showLangSelector = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (lang.code == currentLang.code) BrightPremiumRed else CarbonCardLight
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = lang.displayName, color = SmoothWhite, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // HERO CINEMATIC CAROUSEL SLIDER
        item {
            HeroSlider(viewModel = viewModel, featured = moviesList.first())
        }

        // INSTAGRAM STORIES VIEW TAPE
        item {
            Column {
                Text(
                    text = Localization.get(currentLang.code, "creators_feed"),
                    color = SmoothWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(storiesList) { story ->
                        StoryBubble(story = story, onClick = { viewModel.viewStory(story) })
                    }
                }
            }
        }

        // CONTINUE WATCHING CARDS (ROOM PERSISTENCE BASED)
        if (continueWatching.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = Localization.get(currentLang.code, "continue_watching"),
                        color = SmoothWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(continueWatching) { history ->
                            ContinueWatchingCard(history = history, onClick = {
                                // Find backing movie/series content
                                val backing = viewModel.getallMainmoviesAndseries().find { it.id == history.contentId }
                                if (backing != null) {
                                    viewModel.selectContent(backing)
                                }
                            })
                        }
                    }
                }
            }
        }

        // TRENDING MOVIES ROW with Glass Cards
        item {
            MediaSection(
                title = Localization.get(currentLang.code, "trending_movies"),
                contentList = moviesList,
                langCode = currentLang.code,
                onSelect = { viewModel.selectContent(it) }
            )
        }

        // TRENDING SERIES ROW
        item {
            MediaSection(
                title = Localization.get(currentLang.code, "trending_series"),
                contentList = seriesList,
                langCode = currentLang.code,
                onSelect = { viewModel.selectContent(it) }
            )
        }

        // ADMIN AND EXTRA ACCESS TRIGGERS (QUICK LINKS)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (currentLang.code == "ar") "أدوات متطورة وقدرات خاصة" else "Specialized Modules",
                    color = DeepBurgundy,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(ABKViewModel.Screen.CREATOR_DASHBOARD) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, tint = BrightPremiumRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Localization.get(currentLang.code, "creator_dashboard"),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = { viewModel.navigateTo(ABKViewModel.Screen.ADMIN) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = AmberGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Localization.get(currentLang.code, "admin_panel"),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// --- STORY BUBBLE INSTAGRAM COMPONENTRY ---
@Composable
fun StoryBubble(story: Story, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(BrightPremiumRed, DeepBurgundy, AmberGold, BrightPremiumRed)
                    ),
                    shape = CircleShape
                )
                .padding(3.dp)
                .background(DarkVibeBg, CircleShape)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = story.creatorAvatar,
                contentDescription = story.creatorName,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = story.creatorName,
            color = SmoothWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(76.dp),
            textAlign = TextAlign.Center
        )
    }
}

// --- CINEMATIC CAROUSEL FEATURE CARD ---
@Composable
fun HeroSlider(viewModel: ABKViewModel, featured: VideoContent) {
    val currentLang by viewModel.currentLanguage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { viewModel.selectContent(featured) }
    ) {
        // MOVIE POSTER BACKDROP
        AsyncImage(
            model = featured.coverUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // GRADIATION GLASS PROTECTION SHIELD
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000), Color(0xFA000000))
                    )
                )
        )

        // TEXT AND DETAILS PANEL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            GlassCard(
                modifier = Modifier.padding(bottom = 8.dp),
                backgroundColor = Color(0x40FF013C),
                strokeColor = BrightPremiumRed,
                cornerRadius = 8f
            ) {
                Text(
                    text = Localization.get(currentLang.code, "premium_tag"),
                    color = SmoothWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }

            Text(
                text = if (currentLang.code == "ar") featured.titleAr else featured.titleEn,
                color = SmoothWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (currentLang.code == "ar") featured.descriptionAr else featured.descriptionEn,
                color = SmoothWhite.copy(alpha = 0.82f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${featured.rating} (${featured.ratingCount} ${if (currentLang.code == "ar") "تقييم" else "ratings"})",
                    color = AmberGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = DarkGreyText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${featured.durationMin} ${if (currentLang.code == "ar") "دقيقة" else "min"}",
                        color = SmoothWhite.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// --- CURATED PERSISTENT WATCH PROGRESS CARD ---
@Composable
fun ContinueWatchingCard(history: WatchHistoryEntity, onClick: () -> Unit) {
    val currentProgress = history.playProgress

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(146.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonCard)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = history.imageUrl,
                contentDescription = history.title,
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // overlay dark film
                        drawRect(Color(0x33000000))
                    },
                contentScale = ContentScale.Crop
            )

            // TRICOLOR PROGRESS SLATER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0xE60A0A0C))
                    .padding(8.dp)
            ) {
                Text(
                    text = history.lastEpisodeName.ifEmpty { history.titleAr },
                    color = SmoothWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = BrightPremiumRed,
                    trackColor = Color(0x2AFFFFFF)
                )
            }

            // RED FLOATING CHIP REACTION BUTTON
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .align(Alignment.Center)
                    .background(BrightPremiumRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = SmoothWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// --- STANDARD MEDIA SECTION ROWS ---
@Composable
fun MediaSection(
    title: String,
    contentList: List<VideoContent>,
    langCode: String,
    onSelect: (VideoContent) -> Unit
) {
    Column {
        Text(
            text = title,
            color = SmoothWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(contentList) { content ->
                MediaItemGlassCard(content = content, langCode = langCode, onSelect = { onSelect(content) })
            }
        }
    }
}

@Composable
fun MediaItemGlassCard(content: VideoContent, langCode: String, onSelect: () -> Unit) {
    val label = if (langCode == "ar") content.titleAr else content.titleEn
    val cat = if (langCode == "ar") content.categoryAr else content.categoryEn

    Column(
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onSelect)
    ) {
        Box(
            modifier = Modifier
                .width(135.dp)
                .height(190.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, GlassAlphaStroke, RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = content.imageUrl,
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // VIP premium flag on card
            if (content.isPremium) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(DeepBurgundy, BrightPremiumRed)
                            ),
                            shape = RoundedCornerShape(bottomEnd = 12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(text = "VIP", color = SmoothWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Glass gradient footer overlay for text safety
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xDD000000))
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = SmoothWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = cat,
            color = DarkGreyText,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


// --- CONTENT DETAIL SCREEN ---
@Composable
fun ContentDetailScreen(viewModel: ABKViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val contentState by viewModel.selectedContent.collectAsState()
    val content = contentState ?: return

    val label = if (currentLang.code == "ar") content.titleAr else content.titleEn
    val desc = if (currentLang.code == "ar") content.descriptionAr else content.descriptionEn
    val director = if (currentLang.code == "ar") content.directorAr else content.dircetorEn
    val writer = if (currentLang.code == "ar") content.writerAr else content.writerEn
    val actors = if (currentLang.code == "ar") content.actorsAr else content.actorsEn

    val isFavFlow = viewModel.isFavorite(content.id).collectAsState(initial = false)

    // Interactive tabs
    var selectedTab by remember { mutableStateOf(0) } // 0 = Info, 1 = Season & Episodes, 2 = Reviews, 3 = Comments
    val comments = remember(content.id) { viewModel.loadCommentsForCurrentContent() }
    val reviews = remember(content.id) { viewModel.loadReviewsForCurrentContent() }

    // Download simulator state
    var downloadProgress by remember { mutableStateOf(-1f) } // -1f = idle, 1.0f = done
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp)
    ) {
        // TOP BACK NAVIGATION BAR
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.background(CarbonCard, CircleShape)
                ) {
                    Icon(
                        imageVector = if (currentLang.isRtl) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SmoothWhite
                    )
                }

                Text(
                    text = if (content.isSeries) "Series" else "Movie",
                    color = BrightPremiumRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { viewModel.toggleFavorite(content, isFavFlow.value) },
                    modifier = Modifier.background(CarbonCard, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavFlow.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Fav",
                        tint = BrightPremiumRed
                    )
                }
            }
        }

        // BACKDROP HERO BANNER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AsyncImage(
                    model = content.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, DarkVibeBg)
                            )
                        )
                )

                // FLOATING PLAY BUTTON FOR CINEMATIC SIMULATION
                IconButton(
                    onClick = { viewModel.startPlayingContent(content) },
                    modifier = Modifier
                        .size(70.dp)
                        .background(BrightPremiumRed, CircleShape)
                        .align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = SmoothWhite,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // GENERAL METRICS HEADER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = label,
                    color = SmoothWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "${content.rating} / 10", color = AmberGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Text(text = "${content.releaseYear}", color = SmoothWhite.copy(alpha = 0.6f), fontSize = 13.sp)
                    Text(text = if (currentLang.code == "ar") content.categoryAr else content.categoryEn, color = BrightPremiumRed, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // STREAM ACTIONS BAR WITH OFFLINE DOWNLOADS PROGRESS BAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.startPlayingContent(content) },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrightPremiumRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = Localization.get(currentLang.code, "watch_now"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // DOWNLOAD INTERACTION CHIP
                    Button(
                        onClick = {
                            if (downloadProgress < 0f) {
                                scope.launch {
                                    downloadProgress = 0.1f
                                    while (downloadProgress < 1.0f) {
                                        delay(300)
                                        downloadProgress += 0.2f
                                    }
                                    downloadProgress = 1.0f
                                    viewModel.triggerNotification(
                                        titleEn = "Download Complete: $label",
                                        titleAr = "اكتمل التنزيل: $label",
                                        descEn = "Awesome offline movie package compiled successfully.",
                                        descAr = "تم تنزيل حزمة الفيلم للمشاهدة بدون إنترنت بنجاح."
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(12.dp),
                        enabled = downloadProgress != 1.0f
                    ) {
                        val dlText = when {
                            downloadProgress >= 1.0f -> "✓ ${Localization.get(currentLang.code, "downloaded")}"
                            downloadProgress >= 0f -> "${(downloadProgress * 100).toInt()}%"
                            else -> Localization.get(currentLang.code, "download")
                        }
                        Text(text = dlText, fontSize = 12.sp)
                    }
                }
            }
        }

        // TABS FOR DEEP INTERACTIVE FEATURES
        item {
            Spacer(modifier = Modifier.height(20.dp))
            val tabs = listOf(
                if (currentLang.code == "ar") "المعلومات" else "Information",
                if (content.isSeries) Localization.get(currentLang.code, "seasons") else "Credits",
                Localization.get(currentLang.code, "rating"),
                Localization.get(currentLang.code, "comments")
            )
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = BrightPremiumRed,
                edgePadding = 16.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }
        }

        // DYNAMIC TAB BODY INJECTION
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> { // INFO TABS
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = desc, color = SmoothWhite.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 21.sp)
                            HorizontalDivider(color = GlassAlphaStroke, modifier = Modifier.padding(vertical = 6.dp))
                            Text(text = "${if (currentLang.code == "ar") "المخرج:" else "Director:"} $director", color = SmoothWhite, fontSize = 13.sp)
                            Text(text = "${if (currentLang.code == "ar") "المؤلف:" else "Writer:"} $writer", color = SmoothWhite, fontSize = 13.sp)
                            Text(text = "${if (currentLang.code == "ar") "طاقم العمل المميز:" else "Cast Crew:"} ${actors.joinToString()}", color = SmoothWhite, fontSize = 13.sp)
                        }
                    }
                    1 -> { // SEASONS AND EPISODES
                        if (content.isSeries && content.seasons.isNotEmpty()) {
                            val activeSeason = content.seasons.first()
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = if (currentLang.code == "ar") activeSeason.titleAr else activeSeason.titleEn,
                                    color = BrightPremiumRed,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                activeSeason.episodes.forEach { episode ->
                                    GlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.startPlayingContent(content, episode) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = episode.imageUrl,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${episode.episodeNumber}. ${if (currentLang.code == "ar") episode.titleAr else episode.titleEn}",
                                                    color = SmoothWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${episode.durationMin} ${if (currentLang.code == "ar") "دقيقة" else "min"}",
                                                    color = DarkGreyText,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = null,
                                                tint = BrightPremiumRed
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = if (currentLang.code == "ar") "تم تجميع هذا الفيلم بنسخة سينمائية عالية الجودة ١٠٨٠." else "This movie is compiled in cinematic quality build.",
                                color = SmoothWhite,
                                fontSize = 13.sp
                            )
                        }
                    }
                    2 -> { // REVIEWS (IMDb STYLE)
                        ReviewsTabContent(viewModel = viewModel, reviews = reviews)
                    }
                    3 -> { // COMMENTS SYSTEM
                        CommentsTabContent(viewModel = viewModel, comments = comments)
                    }
                }
            }
        }
    }
}

// --- REVIEW COMPOSABLE PANEL ---
@Composable
fun ReviewsTabContent(viewModel: ABKViewModel, reviews: List<Review>) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    var ratingSlider by remember { mutableStateOf(10f) }
    var reviewTitle by remember { mutableStateOf("") }
    var reviewText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Star feedback rating generator
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (currentLang.code == "ar") "قيم هذا العمل وسجل مراجعتك" else "Submit Your Cinematic Review",
                    color = BrightPremiumRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "${if (currentLang.code == "ar") "التقييم المستحق:" else "Rating Grade:"} ${ratingSlider.toInt()}/10", color = AmberGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = ratingSlider,
                        onValueChange = { ratingSlider = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = AmberGold, activeTrackColor = AmberGold),
                        modifier = Modifier.width(140.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = reviewTitle,
                    onValueChange = { reviewTitle = it },
                    label = { Text(text = Localization.get(currentLang.code, "review_title"), color = DarkGreyText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightPremiumRed, unfocusedTextColor = SmoothWhite, focusedTextColor = SmoothWhite)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text(text = Localization.get(currentLang.code, "placeholder_review"), color = DarkGreyText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightPremiumRed, unfocusedTextColor = SmoothWhite, focusedTextColor = SmoothWhite)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (reviewTitle.isNotEmpty() && reviewText.isNotEmpty()) {
                            viewModel.submitUserReview(ratingSlider.toInt(), reviewTitle, reviewText)
                            reviewTitle = ""
                            reviewText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightPremiumRed),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = Localization.get(currentLang.code, "submit"), color = SmoothWhite, fontSize = 12.sp)
                }
            }
        }

        reviews.forEach { r ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = r.author, color = BrightPremiumRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "${r.rating}/10", color = AmberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = r.title, color = SmoothWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                    Text(text = r.text, color = SmoothWhite.copy(alpha = 0.76f), fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
        }
    }
}

// --- COMMENTS INTERACTIVE FORUM ---
@Composable
fun CommentsTabContent(viewModel: ABKViewModel, comments: List<Comment>) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    var commentField by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentField,
                onValueChange = { commentField = it },
                placeholder = { Text(text = Localization.get(currentLang.code, "placeholder_comment"), color = DarkGreyText) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightPremiumRed, unfocusedTextColor = SmoothWhite, focusedTextColor = SmoothWhite)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (commentField.isNotEmpty()) {
                        viewModel.submitUserComment(commentField)
                        commentField = ""
                    }
                },
                modifier = Modifier.background(BrightPremiumRed, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = SmoothWhite)
            }
        }

        comments.forEach { c ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = c.userAvatar,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = c.userName, color = SmoothWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text(text = if (currentLang.code == "ar") c.timeAgoAr else c.timeAgoEn, color = DarkGreyText, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = c.commentText, color = SmoothWhite, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.likeUserComment(c.id) }
                        ) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = BrightPremiumRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${c.likesCount} ${if (currentLang.code == "ar") "إعجاب" else "likes"}", color = DarkGreyText, fontSize = 11.sp)
                        }

                        // Report trigger
                        Text(
                            text = if (currentLang.code == "ar") "إبلاغ" else "Report",
                            color = DeepBurgundy,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable {
                                viewModel.triggerNotification(
                                    titleEn = "Comment reported",
                                    titleAr = "تم إرسال بلاغ عن التعليق للمراجعة",
                                    descEn = "Our admin moderators will audit. Thank you.",
                                    descAr = "سيتم تدقيق التعليق بواسطة إدارة النظام للمحافظة على أمان المنصة."
                                )
                            }
                        )
                    }

                    // Replies if present
                    c.replies.forEach { r ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 20.dp)
                                .background(CarbonCardLight, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = r.userName, color = BrightPremiumRed, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = r.text, color = SmoothWhite, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- SOCIAL COMMUNITY SCREEN (INSTAGRAM SOCIAL LAYOUT) ---
@Composable
fun CommunityAndStoriesScreen(viewModel: ABKViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val creators = remember { viewModel.getallCreators() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = Localization.get(currentLang.code, "creators_feed"),
                color = SmoothWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = if (currentLang.code == "ar") "تواصل مع مخرجينك ونقادك المفضلين وتابع أعمالهم" else "Connect with verified directors and check their feed",
                color = DarkGreyText,
                fontSize = 12.sp
            )
        }

        items(creators) { creator ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectCreatorForChat(creator) }
            ) {
                Column {
                    // Profile cover
                    AsyncImage(
                        model = creator.coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        contentScale = ContentScale.Crop
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Avatar Overlapping Card margin
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(BrightPremiumRed, CircleShape)
                                .padding(2.dp)
                                .background(CarbonCard, CircleShape)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = creator.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (currentLang.code == "ar") creator.nameAr else creator.nameEn,
                                    color = SmoothWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (creator.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified badge",
                                        tint = BrightPremiumRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(text = creator.handle, color = BrightPremiumRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (currentLang.code == "ar") creator.bioAr else creator.bioEn,
                                color = SmoothWhite.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Bottom info metrics
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CarbonCardLight)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${creator.followersCount} ${Localization.get(currentLang.code, "followers")}",
                            color = SmoothWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { viewModel.selectCreatorForChat(creator) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightPremiumRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 2.dp, horizontal = 12.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = SmoothWhite, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = Localization.get(currentLang.code, "direct_message"), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}


// --- DIRECT DM PRIVATE MESSENGER SCREEN ---
@Composable
fun DirectChatScreen(viewModel: ABKViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val creatorState by viewModel.selectedCreator.collectAsState()
    val creator = creatorState ?: return
    val messages by viewModel.chatMessages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to bottom on message receive
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            delay(100)
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp)
    ) {
        // TOP CONTACT HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CarbonCard)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(
                    imageVector = if (currentLang.isRtl) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = SmoothWhite
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            AsyncImage(
                model = creator.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (currentLang.code == "ar") creator.nameAr else creator.nameEn,
                        color = SmoothWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (creator.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = null, tint = BrightPremiumRed, modifier = Modifier.size(14.dp))
                    }
                }
                Text(
                    text = if (isTyping) Localization.get(currentLang.code, "typing") else creator.handle,
                    color = if (isTyping) BrightPremiumRed else DarkGreyText,
                    fontSize = 11.sp,
                    fontWeight = if (isTyping) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // CONVERSATION FLOW LAYOUT
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == "me"
                val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                val bubbleColor = if (isMe) BrightPremiumRed else CarbonCardLight

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = alignment
                ) {
                    Column(
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        GlassCard(
                            backgroundColor = bubbleColor,
                            strokeColor = if (isMe) BrightPremiumRed.copy(alpha = 0.3f) else GlassAlphaStroke,
                            cornerRadius = 14f,
                            modifier = Modifier.widthIn(max = 250.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (msg.mediaType == "audio") {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = SmoothWhite)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = msg.messageText, color = SmoothWhite, fontSize = 12.sp)
                                    }
                                } else {
                                    Text(text = msg.messageText, color = SmoothWhite, fontSize = 13.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Typing feedback node
            if (isTyping) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        Card(colors = CardDefaults.cardColors(containerColor = CarbonCardLight)) {
                            Text(text = "...", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = BrightPremiumRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // CONTROLS CHAT WRITER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkVibeBg)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.sendVoiceMessage() },
                modifier = Modifier.background(CarbonCard, CircleShape)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = BrightPremiumRed)
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text(text = Localization.get(currentLang.code, "send_message"), color = DarkGreyText) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightPremiumRed, focusedTextColor = SmoothWhite, unfocusedTextColor = SmoothWhite)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textInput.isNotEmpty()) {
                        viewModel.sendMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .background(BrightPremiumRed, CircleShape)
                    .testTag("chat_send_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = SmoothWhite)
            }
        }
    }
}


// --- DIGITAL WALLET (ABK WALLET SYSTEM) ---
@Composable
fun DigitalWalletScreen(viewModel: ABKViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val balance by viewModel.walletBalance.collectAsState()
    val txs by viewModel.transactionsList.collectAsState()

    var showDepositDialog by remember { mutableStateOf(false) }
    var depositAmount by remember { mutableStateOf("50") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = Localization.get(currentLang.code, "wallet"),
                color = SmoothWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }

        // METALLIC GLASS DEBIT CARD SHIELD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(DeepBurgundy, BrightPremiumRed, AmberGold)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(2.dp, SmoothWhite.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ABK VIP Wallet", color = SmoothWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "VISA / PAYPAL", color = SmoothWhite.copy(alpha = 0.7f), fontSize = 11.sp)
                    }

                    Column {
                        Text(text = Localization.get(currentLang.code, "current_balance"), color = SmoothWhite.copy(alpha = 0.76f), fontSize = 12.sp)
                        Text(text = "$${String.format("%.2f", balance)}", color = SmoothWhite, fontSize = 34.sp, fontWeight = FontWeight.Black)
                    }

                    Text(text = "**** **** **** 2026", color = SmoothWhite.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }
        }

        // QUICK TRANSACTION BUTTON ACTIONS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showDepositDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightPremiumRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = SmoothWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = Localization.get(currentLang.code, "deposit"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        // Withdraw
                        viewModel.withdrawFunds(30.0, "SA800049281")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarbonCardLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ArrowOutward, contentDescription = null, tint = SmoothWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = Localization.get(currentLang.code, "withdraw"), fontSize = 12.sp)
                }
            }
        }

        // PREMIUM VIP OFFER CARDS
        item {
            Text(
                text = Localization.get(currentLang.code, "premium_sub_plans"),
                color = SmoothWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                VipSubscriptionOptionCard(
                    title = "Monthly VIP Premium",
                    titleAr = "الاشتراك الشهري البريميوم VIP",
                    price = 8.99,
                    benefitsAr = "بث بدقة 4K • ميزات دردشة صانعي المحتوى • قصص حصرية",
                    benefitsEn = "4K Cinema Streaming • Chat Room access • Stories feed",
                    onBuy = { viewModel.buyVipSubscription(8.99, 1) }
                )
                VipSubscriptionOptionCard(
                    title = "Annual Gold Pass",
                    titleAr = "الممر الذهبي السنوي الشامل",
                    price = 79.99,
                    benefitsAr = "وفّر ٢٥٪ • ترقية أولوية توثيق الحساب • دعم مباشر على مدار الساعة",
                    benefitsEn = "Save 25% • High-priority verification badge • Live help 24/7",
                    onBuy = { viewModel.buyVipSubscription(79.99, 12) }
                )
            }
        }

        // TRANS LOGS
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (currentLang.code == "ar") "سجل الحساب والعمليات" else "Transaction statement", color = SmoothWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (txs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard)
                ) {
                    Text(
                        text = if (currentLang.code == "ar") "سجل العمليات فارغ. شروحات المحفظة تظهر هنا." else "Statements are empty currently.",
                        color = DarkGreyText,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(txs) { tx ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (currentLang.code == "ar") tx.descriptionAr else tx.description,
                                color = SmoothWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tx.type.uppercase(),
                                color = if (tx.type == "deposit") Color.Green else BrightPremiumRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "${if (tx.type == "deposit") "+" else "-"}$${String.format("%.2f", tx.amount)}",
                            color = if (tx.type == "deposit") Color.Green else SmoothWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Interactive deposit dialog simulator
    if (showDepositDialog) {
        AlertDialog(
            onDismissRequest = { showDepositDialog = false },
            containerColor = CarbonCard,
            title = { Text(text = Localization.get(currentLang.code, "deposit"), color = SmoothWhite) },
            text = {
                Column {
                    Text(text = if (currentLang.code == "ar") "اختر أو اكتب قيمة شحن رصيد المحفظة ($):" else "Enter amount to deposit:", color = SmoothWhite.copy(0.7f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = depositAmount,
                        onValueChange = { depositAmount = it },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightPremiumRed, focusedTextColor = SmoothWhite),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = depositAmount.toDoubleOrNull() ?: 50.0
                        viewModel.depositFunds(amt, "Mastercard")
                        showDepositDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightPremiumRed)
                ) {
                    Text(text = Localization.get(currentLang.code, "submit"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositDialog = false }) {
                    Text(text = Localization.get(currentLang.code, "dismiss"), color = SmoothWhite)
                }
            }
        )
    }
}


@Composable
fun VipSubscriptionOptionCard(
    title: String,
    titleAr: String,
    price: Double,
    benefitsAr: String,
    benefitsEn: String,
    onBuy: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = CarbonCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titleAr, color = SmoothWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = title, color = BrightPremiumRed, fontSize = 11.sp, fontWeight = FontWeight.Thin)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = benefitsAr, color = DarkGreyText, fontSize = 10.sp, lineHeight = 13.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "$$price", color = AmberGold, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onBuy,
                    colors = ButtonDefaults.buttonColors(containerColor = BrightPremiumRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(text = "VIP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// --- VERIFICATION AND LOCAL DICTIONARY TOGGLE SETTINGS ---
@Composable
fun VerificationScreen(viewModel: ABKViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()

    val pending by viewModel.verificationPending.collectAsState()
    val isVerifiedBadge by viewModel.isUserVerifiedBadge.collectAsState()
    val notificationsList by viewModel.notifications.collectAsState()

    var fullNameInput by remember { mutableStateOf("") }
    var documentTypeInput by remember { mutableStateOf("National Identity Card") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = Localization.get(currentLang.code, "identity_verification"),
                color = SmoothWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = if (currentLang.code == "ar") "توثيق الحساب لطلب شارة النجمة والتوثيق الزرقاء" else "Verification suite for creators",
                color = DarkGreyText,
                fontSize = 12.sp
            )
        }

        // IDENTITY FORM FOR BLUE VERIFIED STARS
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (currentLang.code == "ar") "تقديم طلب تدقيق فوري" else "Submit identity file",
                        color = BrightPremiumRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = fullNameInput,
                        onValueChange = { fullNameInput = it },
                        label = { Text(text = Localization.get(currentLang.code, "fullname"), color = DarkGreyText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SmoothWhite, unfocusedTextColor = SmoothWhite)
                    )

                    OutlinedTextField(
                        value = documentTypeInput,
                        onValueChange = { documentTypeInput = it },
                        label = { Text(text = Localization.get(currentLang.code, "doc_type"), color = DarkGreyText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SmoothWhite, unfocusedTextColor = SmoothWhite)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val btnText = when {
                        isVerifiedBadge -> "✓ Verified Live Star Account"
                        pending -> "Verifying files..."
                        else -> Localization.get(currentLang.code, "submit_verification_btn")
                    }

                    Button(
                        onClick = {
                            if (fullNameInput.isNotEmpty()) {
                                viewModel.submitVerification(fullNameInput, documentTypeInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isVerifiedBadge) Color.Green else BrightPremiumRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isVerifiedBadge && !pending
                    ) {
                        Text(text = btnText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SYSTEM NOTIFICATIONS FEED TAPE
        item {
            Text(text = if (currentLang.code == "ar") "الإشعارات الواردة بالنظام" else "Recieved system alerts", color = SmoothWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        if (notificationsList.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = CarbonCard)) {
                    Text(text = "No notifications recived yet.", color = DarkGreyText, modifier = Modifier.padding(14.dp), fontSize = 12.sp)
                }
            }
        } else {
            items(notificationsList) { not ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BrightPremiumRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (currentLang.code == "ar") not.titleAr else not.titleEn,
                                color = SmoothWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentLang.code == "ar") not.descriptionAr else not.descriptionEn,
                                color = SmoothWhite.copy(0.74f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


// --- CREATOR STUDIO / PUBLISHER CONSOLE SCREEN ---
@Composable
fun CreatorDashboardScreen(viewModel: ABKViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val drafts by viewModel.creatorDrafts.collectAsState()

    var activePublishTitleEn by remember { mutableStateOf("") }
    var activePublishTitleAr by remember { mutableStateOf("") }
    var activeDescEn by remember { mutableStateOf("") }
    var activeDescAr by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf("Drama") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = Localization.get(currentLang.code, "creator_dashboard"),
                color = SmoothWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = if (currentLang.code == "ar") "استوديو رفع الأفلام وإحصائيات تسييل الأرباح" else "Studio panel to upload cinema and monetize watchtimes",
                color = DarkGreyText,
                fontSize = 12.sp
            )
        }

        // STATS METRICS SLATE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = Localization.get(currentLang.code, "creator_earnings"), color = DarkGreyText, fontSize = 11.sp)
                        Text(text = "$2,450.00", color = SmoothWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "+12% this week", color = Color.Green, fontSize = 9.sp)
                    }
                }

                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = Localization.get(currentLang.code, "watch_time"), color = DarkGreyText, fontSize = 11.sp)
                        Text(text = "48.2 hrs", color = SmoothWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "+48% watch rate", color = Color.Green, fontSize = 9.sp)
                    }
                }
            }
        }

        // SIMULATED UPLOADER ENGINE
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = Localization.get(currentLang.code, "publish_title"),
                        color = BrightPremiumRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = activePublishTitleAr,
                        onValueChange = { activePublishTitleAr = it },
                        label = { Text(text = "العنوان باللغة العربية", color = DarkGreyText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SmoothWhite)
                    )

                    OutlinedTextField(
                        value = activePublishTitleEn,
                        onValueChange = { activePublishTitleEn = it },
                        label = { Text(text = "Title (English)", color = DarkGreyText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SmoothWhite)
                    )

                    OutlinedTextField(
                        value = activeDescAr,
                        onValueChange = { activeDescAr = it },
                        label = { Text(text = "الوصف باللغة العربية", color = DarkGreyText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SmoothWhite)
                    )

                    OutlinedTextField(
                        value = activeDescEn,
                        onValueChange = { activeDescEn = it },
                        label = { Text(text = "Description (English)", color = DarkGreyText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SmoothWhite)
                    )

                    OutlinedTextField(
                        value = activeCategory,
                        onValueChange = { activeCategory = it },
                        label = { Text(text = "Category / Genre", color = DarkGreyText) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = SmoothWhite)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (activePublishTitleAr.isNotEmpty() && activePublishTitleEn.isNotEmpty()) {
                                viewModel.uploadCreatorVideo(
                                    activePublishTitleEn,
                                    activePublishTitleAr,
                                    activeDescEn,
                                    activeDescAr,
                                    activeCategory,
                                    false,
                                    120
                                )
                                activePublishTitleAr = ""
                                activePublishTitleEn = ""
                                activeDescAr = ""
                                activeDescEn = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightPremiumRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = if (currentLang.code == "ar") "نشر وبث الفيلم الآن" else "Publish Video", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // YOUR UPLOAD ARCHIVE TAPE
        item {
            Text(text = if (currentLang.code == "ar") "أرشيف فيديوهاتك المنشورة" else "Your published archive", color = SmoothWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        if (drafts.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = CarbonCard)) {
                    Text(text = if (currentLang.code == "ar") "لا توجد فيديوهات مرفوعة حتى الآن. قم بالرفع أعلاه لتجربة المحاكاة الكاملة." else "Empty published archive.", color = DarkGreyText, modifier = Modifier.padding(14.dp), fontSize = 12.sp)
                }
            }
        } else {
            items(drafts) { draft ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrightPremiumRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MovieFilter, contentDescription = null, tint = SmoothWhite)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = if (currentLang.code == "ar") draft.titleAr else draft.titleEn, color = SmoothWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = draft.categoryEn, color = DarkGreyText, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}


// --- ADMIN SYSTEM CONTROL PANEL ---
@Composable
fun AdminDashboardScreen(viewModel: ABKViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val reports by viewModel.reportedComments.collectAsState()

    var broadcastTitleAr by remember { mutableStateOf("") }
    var broadcastTitleEn by remember { mutableStateOf("") }
    var broadcastBodyAr by remember { mutableStateOf("") }
    var broadcastBodyEn by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = Localization.get(currentLang.code, "admin_panel"),
                color = SmoothWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = if (currentLang.code == "ar") "لوحة مراقبة وإشراف وتوزيع الإشعارات للمسؤول" else "Audit reports and broadcast push notifications to network",
                color = DarkGreyText,
                fontSize = 12.sp
            )
        }

        // GLOBAL STATS CORE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = Localization.get(currentLang.code, "global_stats"), color = AmberGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Active Streams", color = SmoothWhite, fontSize = 12.sp)
                        Text(text = "14,502 users", color = BrightPremiumRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = GlassAlphaStroke, modifier = Modifier.padding(vertical = 5.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total System Revenue", color = SmoothWhite, fontSize = 12.sp)
                        Text(text = "$482,900.50", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // REPORTED USERS AND TRASH CONTROL TAPE
        item {
            Text(text = Localization.get(currentLang.code, "reports"), color = SmoothWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        if (reports.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = CarbonCard)) {
                    Text(text = "All reported logs resolved successfully.", color = Color.Green, modifier = Modifier.padding(14.dp), fontSize = 12.sp)
                }
            }
        } else {
            items(reports) { rep ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Author: ${rep.author}", color = SmoothWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "Reason: ${rep.reason}", color = BrightPremiumRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = rep.snippet, color = SmoothWhite.copy(0.7f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { viewModel.dismissReport(rep.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CarbonCardLight),
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(text = Localization.get(currentLang.code, "dismiss"), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // BROADCAST NOTIFICATION CREATOR
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = Localization.get(currentLang.code, "broadcast_notice"),
                        color = BrightPremiumRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = broadcastTitleAr,
                        onValueChange = { broadcastTitleAr = it },
                        label = { Text(text = "عنوان البث العام (عربي)", color = DarkGreyText) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = broadcastTitleEn,
                        onValueChange = { broadcastTitleEn = it },
                        label = { Text(text = "Broadcast Title (English)", color = DarkGreyText) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = broadcastBodyAr,
                        onValueChange = { broadcastBodyAr = it },
                        label = { Text(text = "نص الرسالة العامة (عربي)", color = DarkGreyText) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = broadcastBodyEn,
                        onValueChange = { broadcastBodyEn = it },
                        label = { Text(text = "Broadcast Body (English)", color = DarkGreyText) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (broadcastTitleAr.isNotEmpty() && broadcastBodyAr.isNotEmpty()) {
                                viewModel.broadcastSystemNotification(
                                    broadcastTitleEn,
                                    broadcastTitleAr,
                                    broadcastBodyEn,
                                    broadcastBodyAr
                                )
                                broadcastTitleAr = ""
                                broadcastTitleEn = ""
                                broadcastBodyAr = ""
                                broadcastBodyEn = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightPremiumRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = if (currentLang.code == "ar") "إرسال وتعميم الإشعار الآن" else "Broadcast Notification", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// --- ADVANCED SEARCH SCREEN ---
@Composable
fun SearchScreen(viewModel: ABKViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredList by viewModel.filteredContent.collectAsState()

    val genreFilter by viewModel.selectedGenreFilter.collectAsState()
    val sortByFilter by viewModel.sortByFilter.collectAsState()

    val genres = listOf("Action", "Sci-Fi", " Drama", "Military")
    val sorts = listOf("Most Popular", "Highest Rated", "Newest")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = Localization.get(currentLang.code, "search"),
                color = SmoothWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }

        // TEXT WRITER SEARCH INPUT
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("primary_search_bar"),
                placeholder = { Text(text = if (currentLang.code == "ar") "ابحث عن الأفلام والمسلسلات أو صناع المحتوى..." else "Search for movies, series...", color = DarkGreyText) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrightPremiumRed) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrightPremiumRed, focusedTextColor = SmoothWhite, unfocusedTextColor = SmoothWhite)
            )
        }

        // FILTER GENRES CHIP STRIP
        item {
            Column {
                Text(text = if (currentLang.code == "ar") "تصفية حسب التصنيف" else "Category Filters", color = SmoothWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = genreFilter == null,
                            onClick = { viewModel.selectGenreFilter(null) },
                            label = { Text(text = if (currentLang.code == "ar") "الكل" else "All") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrightPremiumRed)
                        )
                    }
                    items(genres) { genre ->
                        FilterChip(
                            selected = genreFilter == genre,
                            onClick = { viewModel.selectGenreFilter(genre) },
                            label = { Text(text = genre) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrightPremiumRed)
                        )
                    }
                }
            }
        }

        // FILTER SORT CHIPS STRIP
        item {
            Column {
                Text(text = if (currentLang.code == "ar") "ترتيب النتائج" else "Sorting", color = SmoothWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sorts.forEach { sort ->
                        FilterChip(
                            selected = sortByFilter == sort,
                            onClick = { viewModel.setSortByFilter(sort) },
                            label = { Text(text = sort) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrightPremiumRed)
                        )
                    }
                }
            }
        }

        // SEARCH GRID BODY INJECTION
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLang.code == "ar") "لا توجد نتائج مطابقة لبحثك الحالي" else "No matching results found.",
                        color = DarkGreyText,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(filteredList) { content ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectContent(content) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = content.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(70.dp, 100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentLang.code == "ar") content.titleAr else content.titleEn,
                                color = SmoothWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentLang.code == "ar") content.categoryAr else content.categoryEn,
                                color = BrightPremiumRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (currentLang.code == "ar") content.descriptionAr else content.descriptionEn,
                                color = SmoothWhite.copy(0.7f),
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 15.sp
                            )
                        }

                        Icon(
                            imageVector = if (currentLang.isRtl) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SmoothWhite
                        )
                    }
                }
            }
        }
    }
}


// --- FULLSCREEN LIVE STORIES VIEWER OVERLAY ---
@Composable
fun FullscreenStoryOverlay(viewModel: ABKViewModel, story: Story) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    var storyProgress by remember { mutableStateOf(0f) }

    // Story progress auto-dismiss timeline ticker
    LaunchedEffect(story.id) {
        storyProgress = 0f
        while (storyProgress < 1.0f) {
            delay(50)
            storyProgress += 0.01f
        }
        viewModel.closeStory()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { viewModel.closeStory() }
    ) {
        AsyncImage(
            model = story.mediaUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // HEADER OVERLAY INFO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            // PROGRESS TICKER METERS
            LinearProgressIndicator(
                progress = { storyProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = BrightPremiumRed,
                trackColor = Color(0x3BFFFFFF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = story.creatorAvatar,
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = story.creatorName, color = SmoothWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = if (currentLang.code == "ar") story.timeAgoAr else story.timeAgoEn, color = DarkGreyText, fontSize = 11.sp)
                    }
                }

                IconButton(
                    onClick = { viewModel.closeStory() },
                    modifier = Modifier.background(Color(0x3FF6F6F6), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = SmoothWhite)
                }
            }
        }

        // FOOTER VIEWER REACTIONS CHIPS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xAA000000))
                    )
                )
                .padding(20.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = SmoothWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${story.viewersCount} ${if (currentLang.code == "ar") "مشاهدة" else "viewers"}",
                        color = SmoothWhite,
                        fontSize = 12.sp
                    )
                }

                // Insta Reactions strip
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "❤️", modifier = Modifier.clickable { viewModel.closeStory() }, fontSize = 20.sp)
                    Text(text = "🔥", modifier = Modifier.clickable { viewModel.closeStory() }, fontSize = 20.sp)
                    Text(text = "👏", modifier = Modifier.clickable { viewModel.closeStory() }, fontSize = 20.sp)
                }
            }
        }
    }
}


// --- CINEMATIC PLAYBACK SCREEN OVERLAY ---
@Composable
fun CinematicPlayerOverlay(viewModel: ABKViewModel, content: VideoContent) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val progress by viewModel.playProgressValue.collectAsState()
    val subtitleLang by viewModel.activeSubtitleLang.collectAsState()
    val subtitleSize by viewModel.activeSubtitleSize.collectAsState()

    var isPaused by remember { mutableStateOf(false) }
    var showPlayerSettings by remember { mutableStateOf(false) }

    // Auto simulated progress stepper if not paused
    LaunchedEffect(isPaused) {
        while (!isPaused) {
            delay(1000)
            val nextProg = progress + 0.015f
            if (nextProg >= 1.0f) {
                viewModel.stopPlayingContentAndSaveProgress()
                break
            } else {
                viewModel.updatePlaybackProgress(nextProg)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // MOVIE COVER LAYER (DARK ATMOSPHERE CINEMA CANVAS)
        AsyncImage(
            model = content.coverUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.Black.copy(0.44f))
        )

        // TOP HEADER TITLE TRACK
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.stopPlayingContentAndSaveProgress() },
                modifier = Modifier.background(Color(0x55111111), CircleShape)
            ) {
                Icon(
                    imageVector = if (currentLang.isRtl) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = SmoothWhite
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (currentLang.code == "ar") content.titleAr else content.titleEn,
                    color = SmoothWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(text = "LIVE SHIELD STREAM", color = BrightPremiumRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = { showPlayerSettings = !showPlayerSettings },
                modifier = Modifier.background(Color(0x55111111), CircleShape)
            ) {
                Icon(Icons.Default.Subtitles, contentDescription = null, tint = SmoothWhite)
            }
        }

        // CINEMA SUBTITLES ENGINE INJECTION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 120.dp)
                .padding(horizontal = 24.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            val textToSpeechAr = listOf(
                "الساموراي: نحن لا نخشى الموت، نحن نصنع مستقبلنا بأيدينا في هذه الصحراء.",
                "الساموراي: أسرار الممالك القديمة محفوظة في رمال الزمن التي لا تخطئ.",
                "الساموراي: تراجع من هنا فالمكان محاط بالأمواج الفائقة للشبكة."
            )
            val textToSpeechEn = listOf(
                "Samurai: We do not fear death, we forge our legacy from sands.",
                "Samurai: The secrets of earlier eras remain safely encrypted in trade routes.",
                "Samurai: Retreat directly, for this grid is locked by highly active task forces."
            )

            // Select subtitle based on progress
            val subIndex = (progress * 3).toInt().coerceIn(0, 2)
            val rawSubText = if (subtitleLang == "العربية") textToSpeechAr[subIndex] else textToSpeechEn[subIndex % 3]

            val textFontSize = when(subtitleSize) {
                "Small" -> 13.sp
                "Large" -> 19.sp
                "Extra Large" -> 23.sp
                else -> 16.sp
            }

            Text(
                text = "\" $rawSubText \"",
                color = SmoothWhite,
                fontSize = textFontSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color(0xB3111113), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        // PLAYER CONTROLS FOOTER PANEL
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xF2000000))
                    )
                )
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            // STEP PROGRESS TIMELINER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "04:12", color = SmoothWhite, fontSize = 11.sp)
                Slider(
                    value = progress,
                    onValueChange = { viewModel.updatePlaybackProgress(it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(thumbColor = BrightPremiumRed, activeTrackColor = BrightPremiumRed),
                    modifier = Modifier.weight(1f)
                )
                Text(text = "52:10", color = SmoothWhite, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ACTION CONTROL BUTTON MATRIX
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.updatePlaybackProgress((progress - 0.1f).coerceIn(0f, 1f)) }) {
                    Icon(Icons.Default.Replay10, contentDescription = null, tint = SmoothWhite, modifier = Modifier.size(30.dp))
                }

                IconButton(
                    onClick = { isPaused = !isPaused },
                    modifier = Modifier
                        .size(60.dp)
                        .background(BrightPremiumRed, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        tint = SmoothWhite,
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(onClick = { viewModel.updatePlaybackProgress((progress + 0.1f).coerceIn(0f, 1f)) }) {
                    Icon(Icons.Default.Forward10, contentDescription = null, tint = SmoothWhite, modifier = Modifier.size(30.dp))
                }
            }
        }

        // SUBTITLES FLOATING CHIP CONFIG CAP
        if (showPlayerSettings) {
            AlertDialog(
                onDismissRequest = { showPlayerSettings = false },
                containerColor = CarbonCard,
                title = { Text(text = Localization.get(currentLang.code, "subtitles"), color = SmoothWhite) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "Change Subtitle Track:", color = SmoothWhite.copy(0.7f), fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            viewModel.subtitlesLanguages.take(3).forEach { s ->
                                Button(
                                    onClick = { viewModel.setSubtitleLang(s) },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (subtitleLang == s) BrightPremiumRed else CarbonCardLight),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(text = s, fontSize = 10.sp)
                                }
                            }
                        }

                        Divider(color = GlassAlphaStroke, modifier = Modifier.padding(vertical = 4.dp))

                        Text(text = "Change Font Sizing:", color = SmoothWhite.copy(0.7f), fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            viewModel.subtitleSizes.forEach { sz ->
                                Button(
                                    onClick = { viewModel.setSubtitleSize(sz) },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (subtitleSize == sz) BrightPremiumRed else CarbonCardLight),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(text = sz, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showPlayerSettings = false },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightPremiumRed)
                    ) {
                        Text(text = "OK")
                    }
                }
            )
        }
    }
}
