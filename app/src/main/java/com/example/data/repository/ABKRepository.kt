package com.example.data.repository

import com.example.data.database.ABKDao
import com.example.data.database.CustomListEntity
import com.example.data.database.DirectMessageEntity
import com.example.data.database.FavoriteEntity
import com.example.data.database.WalletTransactionEntity
import com.example.data.database.WatchHistoryEntity
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ABKRepository(private val dao: ABKDao) {

    // --- STATIC CINEMATIC MEDIA POOL ---
    val movies: List<VideoContent> = listOf(
        VideoContent(
            id = "m1",
            titleEn = "The Black Samurai",
            titleAr = "الساموراي الأسود",
            descriptionEn = "An elite warrior rises from shadows to protect ancient kingdoms from deep dark invaders.",
            descriptionAr = "محارب نخبة ينهض من بين الظلال لحماية الممالك القديمة من غزاوة الظلام الدامس في ملحمة سينمائية مذهلة.",
            imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=500&q=80",
            coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1000&q=80",
            rating = 9.2,
            ratingCount = 2840,
            isSeries = false,
            categoryEn = "Action / Epic",
            categoryAr = "أكشن / ملحمة",
            durationMin = 142,
            releaseYear = 2025,
            isPremium = false
        ),
        VideoContent(
            id = "m2",
            titleEn = "Dunes of Destiny",
            titleAr = "كثبان القدر",
            descriptionEn = "In the heart of the Rub' al Khali, a cosmic artifact yields power beyond mortal comprehension.",
            descriptionAr = "في قلب الربع الخالي، تمنح قطعة أثرية كونية قوة تفوق استيعاب البشر، وتبدأ الصراعات الملحمية للسيطرة عليها.",
            imageUrl = "https://images.unsplash.com/photo-1547234935-80c7145ec969?w=500&q=80",
            coverUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=1000&q=80",
            rating = 8.9,
            ratingCount = 1920,
            isSeries = false,
            categoryEn = "Sci-Fi / Adventure",
            categoryAr = "خيال علمي / مغامرة",
            durationMin = 165,
            releaseYear = 2024,
            isPremium = true
        ),
        VideoContent(
            id = "m3",
            titleEn = "Underworld: Riyadh",
            titleAr = "العالم السفلي: الرياض",
            descriptionEn = "An investigative detective is caught in a web of underground corporate secrets and cyber crime.",
            descriptionAr = "مفتش تحريات يقع وسط شبكة معقدة من أسرار الشركات الكبرى وجرائم الأمن السيبراني في الرياض الساحرة ليلاً.",
            imageUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=500&q=80",
            coverUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=1000&q=80",
            rating = 8.5,
            ratingCount = 1420,
            isSeries = false,
            categoryEn = "Thriller / Suspense",
            categoryAr = "تشويق / إثارة",
            durationMin = 118,
            releaseYear = 2025,
            isPremium = false
        ),
        VideoContent(
            id = "m4",
            titleEn = "Kingdom of Sands",
            titleAr = "مملكة الرمال",
            descriptionEn = "An inspiring historical drama detailing the resilience and cultural unity of desert clans in earlier eras.",
            descriptionAr = "دراما تاريخية ملهمة تفصل تفاصيل الصمود والوحدة الثقافية لعشائر الصحراء العريقة في العصور السالفة.",
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&q=80",
            coverUrl = "https://images.unsplash.com/photo-1473116763269-255ea7b2f5f1?w=1000&q=80",
            rating = 9.4,
            ratingCount = 3710,
            isSeries = false,
            categoryEn = "Historical Drama",
            categoryAr = "دراما تاريخية",
            durationMin = 180,
            releaseYear = 2023,
            isPremium = true
        ),
        VideoContent(
            id = "m5",
            titleEn = "Al-Kamin: The Final Siege",
            titleAr = "الكمين: الحصار الأخير",
            descriptionEn = "A rescue unit must execute an intensive mission through hostile mountain terrain under heavy cover of dark.",
            descriptionAr = "يجب على وحدة إنقاذ تنفيذ مهمة مكثفة عبر تضاريس جبلية وعرة تحت غطاء كثيف من الظلام لإنقاذ رهينة مهمة.",
            imageUrl = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=500&q=80",
            coverUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1000&q=80",
            rating = 8.7,
            ratingCount = 1050,
            isSeries = false,
            categoryEn = "War / Military",
            categoryAr = "حربي / عسكري",
            durationMin = 135,
            releaseYear = 2024,
            isPremium = false
        )
    )

    val series: List<VideoContent> = listOf(
        VideoContent(
            id = "s1",
            titleEn = "Sands of Time",
            titleAr = "رمال الزمن",
            descriptionEn = "A multi-generational crime saga unfolding across beautiful desert oasis trade routes.",
            descriptionAr = "ملحمة جريمة عبر الأجيال تدور أحداثها حول طرق تجارة الواحات الصحراوية الجميلة وتنافس العائلات الكبرى.",
            imageUrl = "https://images.unsplash.com/photo-1472214222541-d510753a4707?w=500&q=80",
            coverUrl = "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=1000&q=80",
            rating = 9.3,
            ratingCount = 4920,
            isSeries = true,
            categoryEn = "Family Drama / Mystery",
            categoryAr = "دراما عائلية / غموض",
            durationMin = 45,
            releaseYear = 2024,
            isPremium = false,
            seasons = listOf(
                Season(
                    seasonNumber = 1,
                    titleEn = "Season 1",
                    titleAr = "الموسم الأول",
                    episodes = listOf(
                        Episode("s1_e1", 1, "The Hidden Oasis", "الواحة المخفية", 45, "A merchant uncovers a mysterious scroll.", "تاجر يكتشف لفافة غامضة تغير مسار حياته.", "https://images.unsplash.com/photo-1472214222541-d510753a4707?w=500&q=80"),
                        Episode("s1_e2", 2, "Shadow Agreements", "اتفاقيات الظل", 48, "Alliances are tested in Riyadh trading posts.", "تختبر التحالفات في مراكز التداول في الرياض.", "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=1000&q=80"),
                        Episode("s1_e3", 3, "Scorched Earth", "الأرض المحروقة", 52, "An explosive season finale that seals destinies.", "نهاية موسم متفجرة تحكم الأقدار بشكل كامل.", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&q=80")
                    )
                )
            )
        ),
        VideoContent(
            id = "s2",
            titleEn = "The Falcon's Eye",
            titleAr = "عين الصقر",
            descriptionEn = "A thrilling modern intelligence opera about special task forces dealing with security.",
            descriptionAr = "أوبرا استخباراتية حديثة ومثيرة تدور حول قوات المهام الخاصة التي تتعامل مع قضايا الأمن القومي المعقدة.",
            imageUrl = "https://images.unsplash.com/photo-1496568818309-53d7c7753022?w=500&q=80",
            coverUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1000&q=80",
            rating = 9.1,
            ratingCount = 2150,
            isSeries = true,
            categoryEn = "Action / Espionage",
            categoryAr = "أكشن / تجسس",
            durationMin = 50,
            releaseYear = 2025,
            isPremium = true,
            seasons = listOf(
                Season(
                    seasonNumber = 1,
                    titleEn = "Season 1",
                    titleAr = "الموسم الأول",
                    episodes = listOf(
                        Episode("s2_e1", 1, "First Contact", "الاتصال الأول", 50, "Agent Khalid identifies an encrypted node.", "العميل خالد يحدد عقدة مشفرة مريبة.", "https://images.unsplash.com/photo-1496568818309-53d7c7753022?w=500&q=80"),
                        Episode("s2_e2", 2, "Infiltration", "التسلل الهادئ", 55, "Entering the high-security bunker undetected.", "الدخول إلى خندق حماية عالي السرية سراً دون كشف.", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1000&q=80")
                    )
                )
            )
        )
    )

    val creators: List<Creator> = listOf(
        Creator(
            id = "c1",
            nameEn = "Youssef Cinema",
            nameAr = "يوسف سينما",
            handle = "@youssef_cine",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80",
            coverUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&q=80",
            isVerified = true,
            followersCount = 425000,
            isFollowingMe = true,
            bioAr = "ناقد وباحث سينمائي، أقدم مراجعات أسبوعية حائزة على جوائز لأحدث أفلام السينما العالمية.",
            bioEn = "Film critic and researcher, presenting award-winning weekly reviews for global cinematic releases.",
            earnings = 15450.0,
            watchTimeHrs = 189200
        ),
        Creator(
            id = "c2",
            nameEn = "Sarah Reviews",
            nameAr = "مراجعات سارة",
            handle = "@sarah_reviews",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&q=80",
            coverUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&q=80",
            isVerified = true,
            followersCount = 189000,
            isFollowingMe = false,
            bioAr = "أعشق تحليل المسلسلات الخيالية وعمل نظريات الأبواب المغلقة. انضموا إليّ لتحليل عين الصقر!",
            bioEn = "I live to analyze sci-fi drama series and formulate fan theories. Connect for The Falcon's Eye analytics!",
            earnings = 6200.0,
            watchTimeHrs = 56000
        ),
        Creator(
            id = "c3",
            nameEn = "ABK Official Creator",
            nameAr = "قناة ABK الرسمية",
            handle = "@abk_official",
            avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&q=80",
            coverUrl = "https://images.unsplash.com/photo-1501183007986-d0d080b147f9?w=500&q=80",
            isVerified = true,
            followersCount = 980000,
            isFollowingMe = true,
            bioAr = "القناة الرسمية والناقلة لفعاليات وإعلانات منصة ABK لعشاق الأفلام والبرامج الحصرية.",
            bioEn = "Official developer and operator network delivering direct events and announcements for ABK TV fans.",
            earnings = 89000.0,
            watchTimeHrs = 1205300
        )
    )

    val stories: List<Story> = listOf(
        Story("st1", "c1", "يوسف سينما", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&q=80", "https://images.unsplash.com/photo-1542204172-e70528091f52?w=500&q=80", false, "منذ ساعتين", "2 hrs ago", 450),
        Story("st2", "c2", "مراجعات سارة", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&q=80", "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&q=80", false, "منذ ٥ ساعات", "5 hrs ago", 201),
        Story("st3", "c3", "قناة ABK الرسمية", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&q=80", "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=500&q=80", false, "منذ ١٠ ساعات", "10 hrs ago", 1200)
    )

    // --- INTERACTIVE IN-MEMORY COMMENTS & REVIEWS STATE ---
    private val _commentsState = MutableStateFlow<Map<String, List<Comment>>>(
        mapOf(
            "m1" to listOf(
                Comment("c_m1_1", "حمد السعيد", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150", "فيلم رائع جداً! السينماتوغرافي خارقة للعادة والتأثر بالثقافة الشرقية ممتاز.", "", "منذ ساعة", "1 hr ago", 42,
                    listOf(CommentReply("r1", "عبدالرحمن", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150", "أتفق معك تمامًا، الإضاءة كانت مذهلة!", "", "منذ نصف ساعة", "30 mins ago"))
                ),
                Comment("c_m1_2", "مريم العتيبي", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150", "حلو بس النهاية كانت غير متوقعة ومفاجئة بشكل غريب للبعض.", "", "منذ ٤ ساعات", "4 hrs ago", 15)
            ),
            "s1" to listOf(
                Comment("c_s1_1", "فهد فيصل", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150", "هذا المسلسل هو الأرقى هذا الموسم! حبكة القصة والعقدة مميزة جداً.", "", "منذ يومين", "2 days ago", 89)
            )
        )
    )
    val commentsState: StateFlow<Map<String, List<Comment>>> = _commentsState.asStateFlow()

    private val _reviewsState = MutableStateFlow<Map<String, List<Review>>>(
        mapOf(
            "m1" to listOf(
                Review("r_m1_1", "عبدالله بن فهد", 10, "تحفة فنية بكل المقاييس السينمائية", "أفضل فيلم أكشن درامي حضرته مؤخرًا. تناسق الموسيقى مع المشاهد حكاية خيالية أخرى.", "", "2026-05-25"),
                Review("r_m1_2", "CinemaCritic99", 8, "Great Epic, highly atmospheric", "The design choice is superb though some story elements at the middle feel slightly slow. Overall, a mandatory watch.", "", "2026-05-28")
            ),
            "s1" to listOf(
                Review("r_s1_1", "ناقد مسلسلات", 9, "البداية المثالية لدراما قوية", "رمال الزمن يعود بنا إلى ذروة الإنتاجات العربية ذات البعد النفسي والتشويقي.", "", "2026-05-30")
            )
        )
    )
    val reviewsState: StateFlow<Map<String, List<Review>>> = _reviewsState.asStateFlow()

    // --- MUTATING OPERATIONS ---

    fun getCommentsForContent(contentId: String): List<Comment> {
        return _commentsState.value[contentId] ?: emptyList()
    }

    fun addComment(contentId: String, author: String, text: String) {
        val current = _commentsState.value.toMutableMap()
        val commentList = (current[contentId] ?: emptyList()).toMutableList()
        val newComment = Comment(
            id = "c_added_${System.currentTimeMillis()}",
            userName = author,
            userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            commentText = text,
            commentTextAr = text,
            timeAgoAr = "الآن",
            timeAgoEn = "Just now",
            likesCount = 0
        )
        commentList.add(0, newComment)
        current[contentId] = commentList
        _commentsState.value = current
    }

    fun likeComment(contentId: String, commentId: String) {
        val current = _commentsState.value.toMutableMap()
        val commentList = (current[contentId] ?: emptyList()).map { comment ->
            if (comment.id == commentId) {
                comment.copy(likesCount = comment.likesCount + 1)
            } else {
                comment
            }
        }
        current[contentId] = commentList
        _commentsState.value = current
    }

    fun getReviewsForContent(contentId: String): List<Review> {
        return _reviewsState.value[contentId] ?: emptyList()
    }

    fun addReview(contentId: String, author: String, rating: Int, title: String, text: String) {
        val current = _reviewsState.value.toMutableMap()
        val reviewList = (current[contentId] ?: emptyList()).toMutableList()
        val newReview = Review(
            id = "rev_added_${System.currentTimeMillis()}",
            author = author,
            rating = rating,
            title = title,
            text = text,
            textAr = text,
            date = "2026-05-31"
        )
        reviewList.add(0, newReview)
        current[contentId] = reviewList
        _reviewsState.value = current
    }


    // --- ROOM DATABASE INTEGRATION TUNNELING ---

    fun getWatchHistory(): Flow<List<WatchHistoryEntity>> = dao.getWatchHistory()

    suspend fun saveWatchProgress(contentId: String, title: String, titleAr: String, imageUrl: String, isSeries: Boolean, progress: Float, episodeName: String = "") {
        val entry = WatchHistoryEntity(
            contentId = contentId,
            title = title,
            titleAr = titleAr,
            imageUrl = imageUrl,
            isSeries = isSeries,
            lastEpisodeName = episodeName,
            playProgress = progress,
            timestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdateWatchProgress(entry)
    }

    suspend fun deleteWatchProgress(contentId: String) = dao.deleteWatchProgress(contentId)

    fun getAllFavorites(): Flow<List<FavoriteEntity>> = dao.getAllFavorites()

    fun isFavorite(contentId: String): Flow<Boolean> = dao.isFavorite(contentId)

    suspend fun toggleFavorite(content: VideoContent, currentIsFav: Boolean) {
        if (currentIsFav) {
            dao.removeFavorite(content.id)
        } else {
            val fav = FavoriteEntity(
                contentId = content.id,
                title = content.titleEn,
                titleAr = content.titleAr,
                imageUrl = content.imageUrl,
                rating = content.rating,
                category = content.categoryEn,
                isSeries = content.isSeries
            )
            dao.addFavorite(fav)
        }
    }

    // Wallet transaction persistence
    fun getTransactions(): Flow<List<WalletTransactionEntity>> = dao.getTransactions()

    suspend fun addTransaction(amount: Double, type: String, description: String, descriptionAr: String) {
        val tx = WalletTransactionEntity(
            amount = amount,
            type = type,
            description = description,
            descriptionAr = descriptionAr,
            timestamp = System.currentTimeMillis()
        )
        dao.insertTransaction(tx)
    }

    // Direct Messages chat database persistence
    fun getMessagesBetween(myId: String, recipientId: String): Flow<List<DirectMessageEntity>> =
        dao.getMessagesBetween(myId, recipientId)

    suspend fun sendMessage(recipientId: String, recipientName: String, text: String, mediaType: String = "text") {
        // save sender's original message
        val myMsg = DirectMessageEntity(
            senderId = "me",
            senderName = "أنا",
            recipientId = recipientId,
            messageText = text,
            mediaType = mediaType,
            timestamp = System.currentTimeMillis(),
            isRead = true
        )
        dao.sendMessage(myMsg)

        // Simulate creative automated typing responder
        // based on custom content creator name
    }

    // Custom List helper
    fun getCustomLists(): Flow<List<CustomListEntity>> = dao.getCustomLists()

    suspend fun createCustomList(name: String, nameAr: String, currentIds: String) {
        val list = CustomListEntity(
            listName = name,
            listNameAr = nameAr,
            contentIds = currentIds
        )
        dao.saveCustomList(list)
    }
}
