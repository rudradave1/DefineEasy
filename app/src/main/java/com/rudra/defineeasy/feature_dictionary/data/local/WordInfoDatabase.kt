package com.rudra.defineeasy.feature_dictionary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rudra.defineeasy.feature_dictionary.data.local.entity.CollectionWordCrossRef
import com.rudra.defineeasy.feature_dictionary.data.local.entity.CustomCollectionEntity
import com.rudra.defineeasy.feature_dictionary.data.local.entity.ReviewHistoryEntity
import com.rudra.defineeasy.feature_dictionary.data.local.entity.SearchHistoryEntity
import com.rudra.defineeasy.feature_dictionary.data.local.entity.WordInfoEntity

@Database(
    entities = [
        WordInfoEntity::class,
        SearchHistoryEntity::class,
        ReviewHistoryEntity::class,
        CustomCollectionEntity::class,
        CollectionWordCrossRef::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WordInfoDatabase: RoomDatabase() {
    abstract val dao: WordInfoDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE wordinfoentity ADD COLUMN audioUrl TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "ALTER TABLE wordinfoentity ADD COLUMN isFavorited INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `SearchHistoryEntity` (
                        `word` TEXT NOT NULL,
                        `searchedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`word`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE wordinfoentity ADD COLUMN repetitions INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE wordinfoentity ADD COLUMN intervalDays INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE wordinfoentity ADD COLUMN easinessFactor REAL NOT NULL DEFAULT 2.5"
                )
                db.execSQL(
                    "ALTER TABLE wordinfoentity ADD COLUMN nextReviewDateEpochDay INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `ReviewHistoryEntity` (
                        `word` TEXT NOT NULL,
                        `rating` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `CustomCollectionEntity` (
                        `name` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `CollectionWordCrossRef` (
                        `collectionId` INTEGER NOT NULL,
                        `word` TEXT NOT NULL,
                        PRIMARY KEY(`collectionId`, `word`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
