package org.bibletranslationtools.docscanner

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import org.bibletranslationtools.docscanner.data.repository.PreferenceRepository

class AndroidPreferenceRepository(private val context: Context) : PreferenceRepository {

    /**
     * Returns an instance of the user preferences.
     * This is just the default shared preferences
     */
    private val prefs: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)

    override fun <T> getPref(key: String, type: Class<T>): T? {
        return getPref(key, type, prefs)
    }

    override fun <T> getPref(key: String, defaultValue: T, type: Class<T>): T {
        return getPref(key, defaultValue, type, prefs)
    }

    override fun <T> setPref(key: String, value: T?, type: Class<T>) {
        setPref(key, value, type, prefs)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T>getPref(
        key: String,
        type: Class<T>,
        sharedPreferences: SharedPreferences
    ): T {
        return when (type) {
            java.lang.String::class.java -> sharedPreferences.getString(key, null)
            java.lang.Integer::class.java -> sharedPreferences.getInt(key, -1)
            java.lang.Long::class.java -> sharedPreferences.getLong(key, -1L)
            java.lang.Float::class.java -> sharedPreferences.getFloat(key, -1F)
            java.lang.Boolean::class.java -> sharedPreferences.getBoolean(key, false)
            else -> null
        } as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T>getPref(
        key: String,
        value: T,
        type: Class<T>,
        sharedPreferences: SharedPreferences
    ): T {
        return when (type) {
            java.lang.String::class.java -> sharedPreferences.getString(key, value as String)
            java.lang.Integer::class.java -> sharedPreferences.getInt(key, value as Int)
            java.lang.Long::class.java -> sharedPreferences.getLong(key, value as Long)
            java.lang.Float::class.java -> sharedPreferences.getFloat(key, value as Float)
            java.lang.Boolean::class.java -> sharedPreferences.getBoolean(key, value as Boolean)
            else -> value as String
        } as T
    }

    private fun <T>setPref(
        key: String,
        value: T?,
        type: Class<T>,
        sharedPreferences: SharedPreferences
    ) {
        sharedPreferences.edit {
            if (value != null) {
                when (type) {
                    java.lang.String::class.java -> putString(key, value as String?)
                    java.lang.Integer::class.java -> putInt(key, value as Int)
                    java.lang.Long::class.java -> putLong(key, value as Long)
                    java.lang.Float::class.java -> putFloat(key, value as Float)
                    java.lang.Boolean::class.java -> putBoolean(key, value as Boolean)
                    else -> apply()
                }
            } else {
                remove(key)
            }
        }
    }
}