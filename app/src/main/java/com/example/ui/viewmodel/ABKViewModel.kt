package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.DirectMessageEntity
import com.example.data.database.FavoriteEntity
import com.example.data.database.WalletTransactionEntity
import com.example.data.database.WatchHistoryEntity
import com.example.data.model.*
import com.example.data.repository.ABKRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ABKViewModel(private val repository: ABKRepository) : ViewModel() {

    // --- APP LANGUAGES SUPPORT ---
    val languages = listOf(
        LanguageConfig("ar", "العربية (RTL)", true),
        LanguageConfig("en", "English (LTR)", false),
        LanguageConfig("fr", "Français", false),
        LanguageConfig("de", "Deutsch", false),
        LanguageConfig("it", "Italiano", false),
        LanguageConfig("pt", "Português", false),
        LanguageConfig("ru", "Русский", false)
    )

    private val _currentLanguage = MutableStateFlow(languages[0]) // default Arabic
    val currentLanguage: StateFlow<LanguageConfig> = _currentLanguage.asStateFlow()

    fun selectLanguage(langCode: String) {
        languages.find { it.code == langCode }?.let {
            _currentLanguage.value = it
        }
    }

    // --- NAVIGATION SYSTEM ---
    enum class Screen {
        HOME,
        CONTENT_DETAIL,
        CREATORS,
        CREATOR_DASHBOARD,
        CHAT,
        WALLET,
        VERIFICATION,
        ADMIN,
        SEARCH
    }

    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenBackstack = MutableStateFlow<List<Screen>>(listOf(Screen.HOME))

    fun navigateTo(screen: Screen) {
        val currentList = _screenBackstack.value.toMutableList()
        currentList.add(screen)
        _screenBackstack.value = currentList
        _currentScreen.value = screen
    }

    fun navigateBack() {
        val currentList = _screenBackstack.value.toMutableList()
        if (currentList.size > 1) {
            currentList.removeAt(currentList.size - 1)
            _screenBackstack.value = currentList
            _currentScreen.value = currentList.last()
        }
    }

    // --- DETAILED VIDEO SELECTION ---
    private val _selectedContent = MutableStateFlow<VideoContent?>(null)
    val selectedContent: StateFlow<VideoContent?> = _selectedContent.asStateFlow()

    fun selectContent(content: VideoContent) {
        _selectedContent.value = content
        navigateTo(Screen.CONTENT_DETAIL)
    }

    // --- FAVORITES (ROM FLOW) ---
    val favoritesList: StateFlow<List<FavoriteEntity>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun isFavorite(contentId: String): Flow<Boolean> = repository.isFavorite(contentId)

    fun toggleFavorite(content: VideoContent, isCurrentlyFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(content, isCurrentlyFav)
        }
    }

    // --- WATCH HISTORY / CONTINUE WATCHING (ROM FLOW) ---
    val continueWatchingList: StateFlow<List<WatchHistoryEntity>> = repository.getWatchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- WALLET AND TRANSACTIONS (ROM FLOW) ---
    val transactionsList: StateFlow<List<WalletTransactionEntity>> = repository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletBalance: StateFlow<Double> = repository.getTransactions()
        .map { list ->
            // Start with a default welcome bonus if empty, otherwise sum them up
            val base = 150.0
            val sum = list.sumOf {
                when (it.type) {
                    "deposit" -> it.amount
                    "withdraw", "subscription", "creator_tip" -> -it.amount
                    else -> 0.0
                }
            }
            base + sum
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 150.00)

    fun depositFunds(amount: Double, method: String) {
        viewModelScope.launch {
            repository.addTransaction(
                amount = amount,
                type = "deposit",
                description = "Funds Deposited via $method",
                descriptionAr = "شحن رصيد المحفظة عبر $method"
            )
        }
    }

    fun withdrawFunds(amount: Double, iban: String) {
        viewModelScope.launch {
            repository.addTransaction(
                amount = amount,
                type = "withdraw",
                description = "Withdrawal to IBAN ending in ...${iban.takeLast(4)}",
                descriptionAr = "سحب نقدي إلى حساب آيبان ينتهي بـ ...${iban.takeLast(4)}"
            )
        }
    }

    fun buyVipSubscription(price: Double, durationMonths: Int) {
        viewModelScope.launch {
            repository.addTransaction(
                amount = price,
                type = "subscription",
                description = "ABK VIP Premium Subscription ($durationMonths Months)",
                descriptionAr = "اشتراك بريميوم ABK VIP متميز ($durationMonths أشهر)"
            )
        }
    }

    fun tipCreator(creator: Creator, amount: Double) {
        viewModelScope.launch {
            repository.addTransaction(
                amount = amount,
                type = "creator_tip",
                description = "Creator tip to ${creator.nameEn}",
                descriptionAr = "دعم صانع المحتوى ${creator.nameAr}"
            )
        }
    }

    // --- DIRECT MESSAGES (ROM FLOW & BOT RESPONDER) ---
    private val _selectedCreator = MutableStateFlow<Creator?>(null)
    val selectedCreator: StateFlow<Creator?> = _selectedCreator.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    val chatMessages: StateFlow<List<DirectMessageEntity>> = _selectedCreator
        .flatMapLatest { creator ->
            if (creator != null) {
                repository.getMessagesBetween("me", creator.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCreatorForChat(creator: Creator) {
        _selectedCreator.value = creator
        navigateTo(Screen.CHAT)
    }

    fun sendMessage(text: String, mediaType: String = "text") {
        val creator = _selectedCreator.value ?: return
        viewModelScope.launch {
            repository.sendMessage(creator.id, creator.nameEn, text, mediaType)

            // Trigger typing indicator and smart mock responder!
            _isTyping.value = true
            delay(1500) // realistic delay
            _isTyping.value = false

            val responsesEn = listOf(
                "Thanks for the support! Make sure to watch my newly uploaded content on ABK TV tonight.",
                "Your feedback motivates me enormously! I am currently working on another exciting project for Riyadh underworld.",
                "Yes! Subtitle adjustments are fully customizable in your settings. Tell me what you think of episode 2!",
                "Amazing! Don't forget to share my custom playlist in your stories."
            )
            val responsesAr = listOf(
                "شكرًا جزيلًا لدعمك الرائع! تأكد من متابعة المحتوى الحصري الجديد الذي قمت برفع للتو على منصة ABK TV الليلة.",
                "كلماتك الطيبة تحفزني كثيرًا! أعمل حالياً على مشروع درامي جديد وتاريخي غاية في الإثارة والتشويق.",
                "نعم بالتأكيد! يمكنك تعديل خطوط الترجمة وألوانها بكل مرونة من إعدادات المشغل. شاركني رأيك في الحلقة القادمة!",
                "رائع جداً! لا تنسى مشاركة قائمتي المخصصة في قصصك اليومية."
            )

            val randomIndex = (responsesEn.indices).random()

            repository.sendMessage(
                recipientId = "me",
                recipientName = creator.nameAr,
                text = responsesAr[randomIndex],
                mediaType = "text"
            )

            // Trigger mock notification for receiving a message in background if chat on another screen
            triggerNotification(
                titleEn = "New message from ${creator.nameEn}",
                titleAr = "رسالة جديدة من ${creator.nameAr}",
                descEn = responsesEn[randomIndex],
                descAr = responsesAr[randomIndex]
            )
        }
    }

    // Voice record simulator
    fun sendVoiceMessage() {
        sendMessage(text = "🎤 Voice Note Simulator (0:08)", mediaType = "audio")
    }

    // --- STATUTORY STORIES VIEWER ---
    private val _activeStory = MutableStateFlow<Story?>(null)
    val activeStory: StateFlow<Story?> = _activeStory.asStateFlow()

    fun viewStory(story: Story) {
        _activeStory.value = story
    }

    fun closeStory() {
        _activeStory.value = null
    }

    // --- CINEMATIC PLAYBACK TRACKING SIMULATOR ---
    private val _activePlayContent = MutableStateFlow<VideoContent?>(null)
    val activePlayContent: StateFlow<VideoContent?> = _activePlayContent.asStateFlow()

    private val _activePlayHeading = MutableStateFlow("")
    val activePlayHeading: StateFlow<String> = _activePlayHeading.asStateFlow()

    private val _activePlayHeadingAr = MutableStateFlow("")
    val activePlayHeadingAr: StateFlow<String> = _activePlayHeadingAr.asStateFlow()

    private val _playProgressValue = MutableStateFlow(0.2f)
    val playProgressValue: StateFlow<Float> = _playProgressValue.asStateFlow()

    // Subtitles controller state
    val subtitlesLanguages = listOf("العربية", "English", "Français", "Deutsch", "Italiano", "Русский")
    private val _activeSubtitleLang = MutableStateFlow("العربية")
    val activeSubtitleLang: StateFlow<String> = _activeSubtitleLang.asStateFlow()

    val subtitleSizes = listOf("Small", "Normal", "Large", "Extra Large")
    private val _activeSubtitleSize = MutableStateFlow("Normal")
    val activeSubtitleSize: StateFlow<String> = _activeSubtitleSize.asStateFlow()

    fun setSubtitleLang(lang: String) { _activeSubtitleLang.value = lang }
    fun setSubtitleSize(size: String) { _activeSubtitleSize.value = size }

    fun startPlayingContent(content: VideoContent, episode: Episode? = null) {
        _activePlayContent.value = content
        if (episode != null) {
            _activePlayHeading.value = "${content.titleEn} - Season 1, Ep ${episode.episodeNumber}"
            _activePlayHeadingAr.value = "${content.titleAr} - الموسم ١، الحلقة ${episode.episodeNumber}"
        } else {
            _activePlayHeading.value = content.titleEn
            _activePlayHeadingAr.value = content.titleAr
        }
        _playProgressValue.value = 0.05f
    }

    fun updatePlaybackProgress(prog: Float) {
        _playProgressValue.value = prog
    }

    fun stopPlayingContentAndSaveProgress() {
        val content = _activePlayContent.value
        if (content != null) {
            viewModelScope.launch {
                repository.saveWatchProgress(
                    contentId = content.id,
                    title = content.titleEn,
                    titleAr = content.titleAr,
                    imageUrl = content.imageUrl,
                    isSeries = content.isSeries,
                    progress = _playProgressValue.value,
                    episodeName = _activePlayHeading.value
                )
                _activePlayContent.value = null
            }
        }
    }

    // --- NOTIFICATION FEED MEMORY STATE ---
    data class NotificationModel(
        val id: String,
        val titleEn: String,
        val titleAr: String,
        val descriptionEn: String,
        val descriptionAr: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _notifications = MutableStateFlow<List<NotificationModel>>(
        listOf(
            NotificationModel("n1", "Welcome to ABK TV!", "مرحباً بك في ABK TV!", "Discover premium films and creators with luxury black and red styling.", "اكتشف الأفلام المتميزة وصناع المحتوى مع واجهات سوداء وحمراء فاخرة."),
            NotificationModel("n2", "The Falcon's Eye Episode 3 upload is imminent", "عين الصقر الحلقة ٣ قريباً جداً", "Sarah Reviews will discuss the final theories live tomorrow.", "سارة للمراجعات ستبدأ مناقشة النظريات النهائية غداً بث مباشر.")
        )
    )
    val notifications: StateFlow<List<NotificationModel>> = _notifications.asStateFlow()

    fun triggerNotification(titleEn: String, titleAr: String, descEn: String, descAr: String) {
        val current = _notifications.value.toMutableList()
        current.add(0, NotificationModel(
            id = "not_${System.currentTimeMillis()}",
            titleEn = titleEn,
            titleAr = titleAr,
            descriptionEn = descEn,
            descriptionAr = descAr
        ))
        _notifications.value = current
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    // --- SEARCH / DISCOVERY FILTERS ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenreFilter = MutableStateFlow<String?>(null)
    val selectedGenreFilter: StateFlow<String?> = _selectedGenreFilter.asStateFlow()

    private val _selectedYearFilter = MutableStateFlow<Int?>(null)
    val selectedYearFilter: StateFlow<Int?> = _selectedYearFilter.asStateFlow()

    private val _sortByFilter = MutableStateFlow("Most Popular") // "Most Popular", "Highest Rated", "Newest"
    val sortByFilter: StateFlow<String> = _sortByFilter.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectGenreFilter(genre: String?) {
        _selectedGenreFilter.value = genre
    }

    fun selectYearFilter(year: Int?) {
        _selectedYearFilter.value = year
    }

    fun setSortByFilter(sorting: String) {
        _sortByFilter.value = sorting
    }

    // Computed filtered list
    val filteredContent: StateFlow<List<VideoContent>> = combine(
        _searchQuery,
        _selectedGenreFilter,
        _selectedYearFilter,
        _sortByFilter
    ) { query, genre, year, sort ->
        val fullList = repository.movies + repository.series
        var resultList = fullList.filter { content ->
            val matchesQuery = content.titleEn.contains(query, ignoreCase = true) ||
                    content.titleAr.contains(query, ignoreCase = true) ||
                    content.descriptionEn.contains(query, ignoreCase = true) ||
                    content.descriptionAr.contains(query, ignoreCase = true)

            val matchesGenre = genre == null ||
                    content.categoryEn.contains(genre, ignoreCase = true) ||
                    content.categoryAr.contains(genre, ignoreCase = true)

            val matchesYear = year == null || content.releaseYear == year

            matchesQuery && matchesGenre && matchesYear
        }

        // Apply sorting
        resultList = when(sort) {
            "Highest Rated" -> resultList.sortedByDescending { it.rating }
            "Newest" -> resultList.sortedByDescending { it.releaseYear }
            else -> resultList.sortedByDescending { it.ratingCount } // Default to most popular
        }

        resultList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- CREATOR PUBLISHING / STUDIO SIMULATOR ---
    private val _creatorDrafts = MutableStateFlow<List<VideoContent>>(emptyList())
    val creatorDrafts: StateFlow<List<VideoContent>> = _creatorDrafts.asStateFlow()

    fun uploadCreatorVideo(titleEn: String, titleAr: String, descEn: String, descAr: String, category: String, isPremium: Boolean, duration: Int) {
        val newUpload = VideoContent(
            id = "cr_upl_${System.currentTimeMillis()}",
            titleEn = titleEn,
            titleAr = titleAr,
            descriptionEn = descEn,
            descriptionAr = descAr,
            imageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500",
            coverUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1000",
            rating = 10.0,
            ratingCount = 1,
            isSeries = false,
            categoryEn = category,
            categoryAr = category,
            durationMin = duration,
            releaseYear = 2026,
            isPremium = isPremium
        )
        val currentDrafts = _creatorDrafts.value.toMutableList()
        currentDrafts.add(0, newUpload)
        _creatorDrafts.value = currentDrafts

        // notification trigger
        triggerNotification(
            titleEn = "New creator content available!",
            titleAr = "محتوى حصري صاعد متاح الآن!",
            descEn = "$titleEn is successfully published to ABK TV.",
            descAr = "تم نشر $titleAr بنجاح على منصة ABK الحصرية."
        )
    }


    // --- VERIFICATION REQUEST RECONCILIATION ---
    private val _verificationPending = MutableStateFlow(false)
    val verificationPending: StateFlow<Boolean> = _verificationPending.asStateFlow()

    private val _isUserVerifiedBadge = MutableStateFlow(false)
    val isUserVerifiedBadge: StateFlow<Boolean> = _isUserVerifiedBadge.asStateFlow()

    fun submitVerification(fullName: String, docType: String) {
        viewModelScope.launch {
            _verificationPending.value = true
            delay(2000) // Simulate security admin vetting process
            _verificationPending.value = false
            _isUserVerifiedBadge.value = true
            triggerNotification(
                titleEn = "Identity Verified Successfully!",
                titleAr = "تم توثيق هويتك بنجاح!",
                descEn = "You are now granted the premium ABK Blue Badge.",
                descAr = "لقد حصلت للتو على شارة التوثيق الزرقاء الفاخرة لمنصتنا."
            )
        }
    }


    // --- ADMIN SYSTEM MONITOR AND AUDITS ---
    private val _reportedComments = MutableStateFlow<List<ReportedComment>>(
        listOf(
            ReportedComment("rep1", "c_m1_2", "مريم العتيبي", "حلو بس النهاية كانت غير متوقعة...", "Spam"),
            ReportedComment("rep2", "c_s1_1", "فهد فيصل", "هذا المسلسل هو الأرقى هذا الموسم!...", "Offensive")
        )
    )
    val reportedComments: StateFlow<List<ReportedComment>> = _reportedComments.asStateFlow()

    fun dismissReport(reportId: String) {
        _reportedComments.value = _reportedComments.value.filter { it.id != reportId }
    }

    fun broadcastSystemNotification(titleEn: String, titleAr: String, descEn: String, descAr: String) {
        triggerNotification(titleEn, titleAr, descEn, descAr)
    }

    fun getallMainmoviesAndseries(): List<VideoContent> {
        return repository.movies + repository.series
    }

    fun getallCreators(): List<Creator> {
        return repository.creators
    }

    fun getallStories(): List<Story> {
        return repository.stories
    }

    fun loadReviewsForCurrentContent(): List<Review> {
        val current = _selectedContent.value ?: return emptyList()
        return repository.getReviewsForContent(current.id)
    }

    fun submitUserReview(rating: Int, title: String, text: String) {
        val current = _selectedContent.value ?: return
        repository.addReview(current.id, "أنا (المشترك)", rating, title, text)
        // Refresh detail flow by forcing state update
        _selectedContent.value = _selectedContent.value?.copy() 
    }

    fun loadCommentsForCurrentContent(): List<Comment> {
        val current = _selectedContent.value ?: return emptyList()
        return repository.getCommentsForContent(current.id)
    }

    fun submitUserComment(text: String) {
        val current = _selectedContent.value ?: return
        repository.addComment(current.id, "أنا (المشترك)", text)
        _selectedContent.value = _selectedContent.value?.copy() // force redraw / trigger flows
    }

    fun likeUserComment(commentId: String) {
        val current = _selectedContent.value ?: return
        repository.likeComment(current.id, commentId)
        _selectedContent.value = _selectedContent.value?.copy()
    }
}

// Data holder config structures
data class LanguageConfig(val code: String, val displayName: String, val isRtl: Boolean)
data class ReportedComment(val id: String, val commentId: String, val author: String, val snippet: String, val reason: String)


// --- VIEWMODEL FACTORY ---
class ABKViewModelFactory(private val repository: ABKRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ABKViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ABKViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
