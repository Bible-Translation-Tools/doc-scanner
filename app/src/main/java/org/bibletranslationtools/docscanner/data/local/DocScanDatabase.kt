package org.bibletranslationtools.docscanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.bibletranslationtools.docscanner.data.local.converter.DateTypeConverter
import org.bibletranslationtools.docscanner.data.local.dao.BookDao
import org.bibletranslationtools.docscanner.data.local.dao.LanguageDao
import org.bibletranslationtools.docscanner.data.local.dao.LevelDao
import org.bibletranslationtools.docscanner.data.local.dao.PdfDao
import org.bibletranslationtools.docscanner.data.local.dao.ProjectDao
import org.bibletranslationtools.docscanner.data.models.Book
import org.bibletranslationtools.docscanner.data.models.Language
import org.bibletranslationtools.docscanner.data.models.Level
import org.bibletranslationtools.docscanner.data.models.Pdf
import org.bibletranslationtools.docscanner.data.models.Project

@Database(
    entities = [
        Pdf::class,
        Project::class,
        Language::class,
        Book::class,
        Level::class
    ],
    version = 1,
    exportSchema = false
)

@TypeConverters(DateTypeConverter::class)
abstract class DocScanDatabase : RoomDatabase() {
    abstract val pdfDao: PdfDao
    abstract val projectDao: ProjectDao
    abstract val languageDao: LanguageDao
    abstract val bookDao: BookDao
    abstract val levelDao: LevelDao

    companion object {

        @Volatile
        private var INSTANCE: DocScanDatabase? = null

        fun getInstance(context: Context): DocScanDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DocScanDatabase::class.java,
                    "doc_scan_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}