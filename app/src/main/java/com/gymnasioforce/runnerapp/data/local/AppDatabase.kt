package com.gymnasioforce.runnerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Base de datos Room local.
 * Version 2: agrega campos serverId y synced para modo offline.
 */
@Database(entities = [RunEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun runDao(): RunDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migracion de v1 a v2: agregar columnas para offline
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Agregar columna serverId (copia del id actual)
                db.execSQL("ALTER TABLE runs ADD COLUMN serverId INTEGER")
                db.execSQL("UPDATE runs SET serverId = id")
                // Agregar columna synced (todas las existentes ya estan sincronizadas)
                db.execSQL("ALTER TABLE runs ADD COLUMN synced INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "runner_app_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
