package org.bibletranslationtools.docscanner.data.repository

import com.russhwolf.settings.Settings
import kotlin.reflect.KClass

interface PreferenceRepository {
    /**
     * Returns the value of a user preference
     * @param key
     */
    fun <T : Any> getPref(key: String, type: KClass<T>): T?

    /**
     * Returns the value of a user preference or the default value
     */
    fun <T : Any> getPref(key: String, defaultValue: T, type: KClass<T>): T

    /**
     * Sets the value of a default preference.
     * @param key
     * @param value if null the value will be removed
     */
    fun <T : Any> setPref(key: String, value: T?, type: KClass<T>)
}

inline fun <reified T : Any> PreferenceRepository.getPref(key: String) =
    getPref(key, T::class)

inline fun <reified T : Any> PreferenceRepository.getPref(key: String, defaultValue: T) =
    getPref(key, defaultValue, T::class)

inline fun <reified T : Any> PreferenceRepository.setPref(key: String, value: T?) =
    setPref(key, value, T::class)

/**
 * Multiplatform implementation backed by [Settings].
 * On Android the [Settings] instance wraps SharedPreferences,
 * on iOS it wraps NSUserDefaults (provided via the platform Koin module).
 */
class SettingsPreferenceRepository(
    private val settings: Settings
) : PreferenceRepository {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getPref(key: String, type: KClass<T>): T? {
        if (!settings.hasKey(key)) return null
        return when (type) {
            String::class -> settings.getStringOrNull(key)
            Int::class -> settings.getIntOrNull(key)
            Long::class -> settings.getLongOrNull(key)
            Float::class -> settings.getFloatOrNull(key)
            Boolean::class -> settings.getBooleanOrNull(key)
            else -> null
        } as T?
    }

    override fun <T : Any> getPref(key: String, defaultValue: T, type: KClass<T>): T {
        return getPref(key, type) ?: defaultValue
    }

    override fun <T : Any> setPref(key: String, value: T?, type: KClass<T>) {
        if (value == null) {
            settings.remove(key)
            return
        }
        when (value) {
            is String -> settings.putString(key, value)
            is Int -> settings.putInt(key, value)
            is Long -> settings.putLong(key, value)
            is Float -> settings.putFloat(key, value)
            is Boolean -> settings.putBoolean(key, value)
        }
    }
}
