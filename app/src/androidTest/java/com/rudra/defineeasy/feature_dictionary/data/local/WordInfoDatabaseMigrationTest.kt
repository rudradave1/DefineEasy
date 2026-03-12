package com.rudra.defineeasy.feature_dictionary.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WordInfoDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val dbName = "word-info-migration-test"

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun migration1To2_opensExistingDatabaseWithoutCrash() {
        val db = FrameworkSQLiteOpenHelperFactory()
            .create(
                androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name(dbName)
                    .callback(
                        object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) = Unit

                            override fun onUpgrade(
                                db: androidx.sqlite.db.SupportSQLiteDatabase,
                                oldVersion: Int,
                                newVersion: Int
                            ) = Unit
                        }
                    )
                    .build()
            )

        db.writableDatabase.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `WordInfoEntity` (
                `word` TEXT NOT NULL,
                `phonetic` TEXT NOT NULL,
                `origin` TEXT NOT NULL,
                `meanings` TEXT NOT NULL,
                `id` INTEGER PRIMARY KEY AUTOINCREMENT
            )
            """.trimIndent()
        )
        db.close()

        Room.databaseBuilder(context, WordInfoDatabase::class.java, dbName)
            .addMigrations(WordInfoDatabase.MIGRATION_1_2)
            .build()
            .openHelper
            .writableDatabase
            .close()
    }
}
