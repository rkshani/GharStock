package com.rk.gharstock.core.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rk.gharstock.R

/**
 * Inserts the default categories and storage places once, when the database is first created.
 * Users can later edit or delete them, and they are not re-added.
 */
class SeedDefaultsCallback(private val context: Context) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        insertNames(db, "categories", R.array.default_categories)
        insertNames(db, "storage_places", R.array.default_storage_places)
    }

    private fun insertNames(db: SupportSQLiteDatabase, table: String, namesRes: Int) {
        context.resources.getStringArray(namesRes).forEach { name ->
            val values = ContentValues().apply { put("name", name) }
            db.insert(table, SQLiteDatabase.CONFLICT_IGNORE, values)
        }
    }
}
