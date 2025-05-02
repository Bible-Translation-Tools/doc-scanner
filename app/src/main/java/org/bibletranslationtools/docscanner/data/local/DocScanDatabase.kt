package org.bibletranslationtools.docscanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.bibletranslationtools.docscanner.data.local.converter.DateTypeConverter
import org.bibletranslationtools.docscanner.data.local.dao.PdfDao
import org.bibletranslationtools.docscanner.data.local.dao.ProjectDao
import org.bibletranslationtools.docscanner.data.models.Pdf
import org.bibletranslationtools.docscanner.data.models.Project

@Database(
    entities = [Pdf::class, Project::class], version = 1, exportSchema = false
)

@TypeConverters(DateTypeConverter::class)
//abstract class
abstract class DocScanDatabase : RoomDatabase() {
    //    dao
    abstract val pdfDao: PdfDao
    abstract val projectDao: ProjectDao

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