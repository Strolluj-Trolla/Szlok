package pl.put.poznan.student.mp160164.szlok.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TrailTime::class, TimerStart::class, Favourite::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timeDao(): TimeDao
    abstract fun startDao(): StartDao
    abstract fun favDao(): FavDao


    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE TimerStart ADD COLUMN time INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE TrailTime ADD COLUMN date TEXT NOT NULL DEFAULT '1.05.2026'")
                db.execSQL("CREATE TABLE Favourite (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `trailId` TEXT NOT NULL)")
            }
        }

        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "szlak_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build().also { INSTANCE = it }
            }
    }
}