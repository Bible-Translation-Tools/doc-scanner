package org.bibletranslationtools.docscanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.bibletranslationtools.docscanner.data.local.converter.DateTypeConverter
import org.bibletranslationtools.docscanner.data.local.dao.PdfDao
import org.bibletranslationtools.docscanner.data.models.PdfEntity


@Database(
    entities = [PdfEntity::class], version = 1, exportSchema = false
)

@TypeConverters(DateTypeConverter::class)
//abstract class
abstract class PdfDatabase : RoomDatabase() {
    //    dao
    abstract val pdfDao: PdfDao

    companion object {

        @Volatile
        private var INSTANCE: PdfDatabase? = null

        fun getInstance(context: Context): PdfDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PdfDatabase::class.java,
                    "pdf_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}