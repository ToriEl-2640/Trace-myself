package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.example.data.model.CatalogOpportunity
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_grants")
data class CachedGrantEntity(
    @PrimaryKey val id: String,
    val title: String,
    val organization: String,
    val fundingAmount: String,
    val deadline: String,
    val category: String,
    val focusArea: String,
    val eligibleRegions: String,
    val description: String,
    val applicationUrl: String,
    val isLiveFeed: Boolean,
    val openDate: String,
    val opportunityNumber: String,
    val fetchedAtMillis: Long
) {
    fun toCatalogOpportunity(): CatalogOpportunity = CatalogOpportunity(
        id = id,
        title = title,
        organization = organization,
        fundingAmount = fundingAmount,
        deadline = deadline,
        category = category,
        focusArea = focusArea,
        eligibleRegions = eligibleRegions,
        description = description,
        applicationUrl = applicationUrl,
        isLiveFeed = isLiveFeed,
        openDate = openDate,
        opportunityNumber = opportunityNumber
    )
}

@Dao
interface CachedGrantDao {
    @Query("SELECT * FROM cached_grants ORDER BY fetchedAtMillis DESC, title ASC")
    fun observeAllLiveGrants(): Flow<List<CachedGrantEntity>>

    @Query("SELECT MAX(fetchedAtMillis) FROM cached_grants")
    fun observeLastSyncMillis(): Flow<Long?>

    @Query("SELECT MAX(fetchedAtMillis) FROM cached_grants")
    suspend fun getLastSyncMillis(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrants(grants: List<CachedGrantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGrant(grant: CachedGrantEntity)

    @Query("DELETE FROM cached_grants")
    suspend fun clearAllGrants()

    @Transaction
    suspend fun replaceAllLiveGrants(grants: List<CachedGrantEntity>) {
        clearAllGrants()
        insertGrants(grants)
    }
}

@Database(entities = [CachedGrantEntity::class], version = 1, exportSchema = false)
abstract class TraceLocalDatabase : RoomDatabase() {
    abstract fun cachedGrantDao(): CachedGrantDao

    companion object {
        @Volatile
        private var INSTANCE: TraceLocalDatabase? = null

        fun getInstance(context: Context): TraceLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TraceLocalDatabase::class.java,
                    "trace_local_cache.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
