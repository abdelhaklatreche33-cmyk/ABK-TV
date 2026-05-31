package com.example.data.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ----------------- ENTITIES -----------------

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val contentId: String,
    val title: String,
    val titleAr: String,
    val imageUrl: String,
    val isSeries: Boolean,
    val lastEpisodeName: String = "",
    val playProgress: Float, // 0.0 to 1.0f
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val contentId: String,
    val title: String,
    val titleAr: String,
    val imageUrl: String,
    val rating: Double,
    val category: String,
    val isSeries: Boolean
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amount: Double,
    val type: String, // "deposit", "withdraw", "subscription", "creator_tip"
    val description: String,
    val descriptionAr: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chats")
data class DirectMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val senderId: String, // "me", "creator_id"
    val senderName: String,
    val recipientId: String,
    val messageText: String,
    val mediaType: String = "text", // "text", "audio", "image"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "custom_lists")
data class CustomListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val listName: String,
    val listNameAr: String,
    val contentIds: String // comma separated list
)


// ----------------- DAO -----------------

@Dao
interface ABKDao {
    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWatchProgress(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE contentId = :contentId")
    suspend fun deleteWatchProgress(contentId: String)

    // Favorites
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE contentId = :contentId)")
    fun isFavorite(contentId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE contentId = :contentId")
    suspend fun removeFavorite(contentId: String)

    // Wallet
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: WalletTransactionEntity)

    // Chats
    @Query("SELECT * FROM chats ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<DirectMessageEntity>>

    @Query("SELECT * FROM chats WHERE (senderId = :id1 AND recipientId = :id2) OR (senderId = :id2 AND recipientId = :id1) ORDER BY timestamp ASC")
    fun getMessagesBetween(id1: String, id2: String): Flow<List<DirectMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun sendMessage(msg: DirectMessageEntity)

    // Custom lists
    @Query("SELECT * FROM custom_lists")
    fun getCustomLists(): Flow<List<CustomListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCustomList(list: CustomListEntity)
}


// ----------------- DATABASE -----------------

@Database(
    entities = [
        WatchHistoryEntity::class,
        FavoriteEntity::class,
        WalletTransactionEntity::class,
        DirectMessageEntity::class,
        CustomListEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ABKDatabase : RoomDatabase() {
    abstract fun dao(): ABKDao

    companion object {
        @Volatile
        private var INSTANCE: ABKDatabase? = null

        fun getDatabase(context: Context): ABKDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ABKDatabase::class.java,
                    "abk_tv_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
