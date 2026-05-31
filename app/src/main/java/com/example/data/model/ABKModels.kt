package com.example.data.model

data class VideoContent(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val imageUrl: String,
    val coverUrl: String,
    val rating: Double,
    val ratingCount: Int,
    val isSeries: Boolean,
    val categoryEn: String,
    val categoryAr: String,
    val durationMin: Int, // for TV, normal episode time
    val releaseYear: Int,
    val dircetorEn: String = "Steven Spielberg",
    val directorAr: String = "ستيفن سبيلبرغ",
    val writerEn: String = "Johnathan Nolan",
    val writerAr: String = "جوناثان نولان",
    val actorsEn: List<String> = listOf("Christian Bale", "Robert Downey Jr", "Scarlett Johansson"),
    val actorsAr: List<String> = listOf("كريستيان بيل", "روبرت داوني جونير", "سكارليت جوهانسون"),
    val views: Int = 14500,
    val isPremium: Boolean = false,
    val seasons: List<Season> = emptyList()
)

data class Season(
    val seasonNumber: Int,
    val titleEn: String,
    val titleAr: String,
    val episodes: List<Episode>
)

data class Episode(
    val id: String,
    val episodeNumber: Int,
    val titleEn: String,
    val titleAr: String,
    val durationMin: Int,
    val descriptionEn: String,
    val descriptionAr: String,
    val imageUrl: String
)

data class Creator(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val handle: String,
    val avatarUrl: String,
    val coverUrl: String,
    val isVerified: Boolean = false,
    val followersCount: Int = 12000,
    val isFollowingMe: Boolean = false,
    val bioAr: String = "منشئ محتوى ومخرج سينمائي ومحب للأفلام الرقمية.",
    val bioEn: String = "Content creator, film director, and digital cinema enthusiast.",
    val earnings: Double = 3400.50,
    val watchTimeHrs: Int = 41200
)

data class Story(
    val id: String,
    val creatorId: String,
    val creatorName: String,
    val creatorAvatar: String,
    val mediaUrl: String,
    val isVideo: Boolean = false,
    val timeAgoAr: String,
    val timeAgoEn: String,
    val viewersCount: Int = 230
)

data class Comment(
    val id: String,
    val userName: String,
    val userAvatar: String,
    val commentText: String,
    val commentTextAr: String = "",
    val timeAgoAr: String,
    val timeAgoEn: String,
    val likesCount: Int = 0,
    val replies: List<CommentReply> = emptyList()
)

data class CommentReply(
    val id: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val textAr: String = "",
    val timeAgoAr: String,
    val timeAgoEn: String
)

data class Review(
    val id: String,
    val author: String,
    val rating: Int, // 1 to 10
    val title: String,
    val text: String,
    val textAr: String = "",
    val date: String
)
