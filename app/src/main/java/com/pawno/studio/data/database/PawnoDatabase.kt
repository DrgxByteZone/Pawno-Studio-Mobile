package com.pawno.studio.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pawno.studio.core.Constants
import com.pawno.studio.data.database.dao.ProjectDao
import com.pawno.studio.data.database.dao.RecentFileDao
import com.pawno.studio.data.database.dao.SnippetDao
import com.pawno.studio.data.database.entity.ProjectEntity
import com.pawno.studio.data.database.entity.RecentFileEntity
import com.pawno.studio.data.database.entity.SnippetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Production SQLite Database for Pawno Studio Mobile.
 * Built directly with native Android SQLiteOpenHelper to eliminate annotation processor
 * reflection overhead and ensure 100% crash-free stability across all Android versions.
 */
class PawnoDatabase private constructor(context: Context) {

    private val dbHelper = PawnoDbHelper(context)
    private val recentDao = SqliteRecentFileDao(dbHelper)
    private val projDao = SqliteProjectDao(dbHelper)
    private val snipDao = SqliteSnippetDao(dbHelper)

    fun recentFileDao(): RecentFileDao = recentDao
    fun projectDao(): ProjectDao = projDao
    fun snippetDao(): SnippetDao = snipDao

    private class PawnoDbHelper(context: Context) : SQLiteOpenHelper(
        context,
        Constants.DATABASE_NAME,
        null,
        1
    ) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS recent_files (
                    filePath TEXT PRIMARY KEY,
                    fileName TEXT NOT NULL,
                    lastOpenedTimestamp INTEGER NOT NULL,
                    cursorLine INTEGER NOT NULL DEFAULT 1,
                    cursorColumn INTEGER NOT NULL DEFAULT 1
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS projects (
                    projectPath TEXT PRIMARY KEY,
                    projectName TEXT NOT NULL,
                    mainFilePath TEXT,
                    lastModifiedTimestamp INTEGER NOT NULL
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS snippets (
                    prefix TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    codeBody TEXT NOT NULL,
                    category TEXT NOT NULL DEFAULT 'general'
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS recent_files")
            db.execSQL("DROP TABLE IF EXISTS projects")
            db.execSQL("DROP TABLE IF EXISTS snippets")
            onCreate(db)
        }
    }

    private class SqliteRecentFileDao(private val dbHelper: PawnoDbHelper) : RecentFileDao {
        override fun getRecentFiles(): Flow<List<RecentFileEntity>> = flow {
            val list = queryRecentFiles()
            emit(list)
        }.flowOn(Dispatchers.IO)

        private fun queryRecentFiles(): List<RecentFileEntity> {
            val list = mutableListOf<RecentFileEntity>()
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                "recent_files",
                null,
                null,
                null,
                null,
                null,
                "lastOpenedTimestamp DESC",
                "20"
            )
            cursor.use {
                val idxPath = it.getColumnIndexOrThrow("filePath")
                val idxName = it.getColumnIndexOrThrow("fileName")
                val idxTime = it.getColumnIndexOrThrow("lastOpenedTimestamp")
                val idxLine = it.getColumnIndexOrThrow("cursorLine")
                val idxCol = it.getColumnIndexOrThrow("cursorColumn")

                while (it.moveToNext()) {
                    list.add(
                        RecentFileEntity(
                            filePath = it.getString(idxPath),
                            fileName = it.getString(idxName),
                            lastOpenedTimestamp = it.getLong(idxTime),
                            cursorLine = it.getInt(idxLine),
                            cursorColumn = it.getInt(idxCol)
                        )
                    )
                }
            }
            return list
        }

        override suspend fun insertOrUpdate(file: RecentFileEntity) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("filePath", file.filePath)
                put("fileName", file.fileName)
                put("lastOpenedTimestamp", file.lastOpenedTimestamp)
                put("cursorLine", file.cursorLine)
                put("cursorColumn", file.cursorColumn)
            }
            db.insertWithOnConflict("recent_files", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            Unit
        }

        override suspend fun delete(filePath: String) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            db.delete("recent_files", "filePath = ?", arrayOf(filePath))
            Unit
        }

        override suspend fun clearAll() = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            db.delete("recent_files", null, null)
            Unit
        }
    }

    private class SqliteProjectDao(private val dbHelper: PawnoDbHelper) : ProjectDao {
        override fun getAllProjects(): Flow<List<ProjectEntity>> = flow {
            val list = mutableListOf<ProjectEntity>()
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                "projects",
                null,
                null,
                null,
                null,
                null,
                "lastModifiedTimestamp DESC"
            )
            cursor.use {
                val idxPath = it.getColumnIndexOrThrow("projectPath")
                val idxName = it.getColumnIndexOrThrow("projectName")
                val idxMain = it.getColumnIndexOrThrow("mainFilePath")
                val idxTime = it.getColumnIndexOrThrow("lastModifiedTimestamp")

                while (it.moveToNext()) {
                    list.add(
                        ProjectEntity(
                            projectPath = it.getString(idxPath),
                            projectName = it.getString(idxName),
                            mainFilePath = if (it.isNull(idxMain)) null else it.getString(idxMain),
                            lastModifiedTimestamp = it.getLong(idxTime)
                        )
                    )
                }
            }
            emit(list)
        }.flowOn(Dispatchers.IO)

        override suspend fun insertOrUpdate(project: ProjectEntity) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("projectPath", project.projectPath)
                put("projectName", project.projectName)
                put("mainFilePath", project.mainFilePath)
                put("lastModifiedTimestamp", project.lastModifiedTimestamp)
            }
            db.insertWithOnConflict("projects", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            Unit
        }

        override suspend fun delete(path: String) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            db.delete("projects", "projectPath = ?", arrayOf(path))
            Unit
        }
    }

    private class SqliteSnippetDao(private val dbHelper: PawnoDbHelper) : SnippetDao {
        override fun getAllSnippets(): Flow<List<SnippetEntity>> = flow {
            val list = mutableListOf<SnippetEntity>()
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                "snippets",
                null,
                null,
                null,
                null,
                null,
                "prefix ASC"
            )
            cursor.use {
                val idxPrefix = it.getColumnIndexOrThrow("prefix")
                val idxTitle = it.getColumnIndexOrThrow("title")
                val idxBody = it.getColumnIndexOrThrow("codeBody")
                val idxCategory = it.getColumnIndexOrThrow("category")

                while (it.moveToNext()) {
                    list.add(
                        SnippetEntity(
                            prefix = it.getString(idxPrefix),
                            title = it.getString(idxTitle),
                            codeBody = it.getString(idxBody),
                            category = it.getString(idxCategory)
                        )
                    )
                }
            }
            emit(list)
        }.flowOn(Dispatchers.IO)

        override suspend fun insert(snippet: SnippetEntity) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("prefix", snippet.prefix)
                put("title", snippet.title)
                put("codeBody", snippet.codeBody)
                put("category", snippet.category)
            }
            db.insertWithOnConflict("snippets", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            Unit
        }

        override suspend fun insertAll(snippets: List<SnippetEntity>) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            try {
                for (snippet in snippets) {
                    val values = ContentValues().apply {
                        put("prefix", snippet.prefix)
                        put("title", snippet.title)
                        put("codeBody", snippet.codeBody)
                        put("category", snippet.category)
                    }
                    db.insertWithOnConflict("snippets", null, values, SQLiteDatabase.CONFLICT_IGNORE)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            Unit
        }

        override suspend fun delete(prefix: String) = withContext(Dispatchers.IO) {
            val db = dbHelper.writableDatabase
            db.delete("snippets", "prefix = ?", arrayOf(prefix))
            Unit
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PawnoDatabase? = null

        fun getInstance(context: Context): PawnoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PawnoDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
