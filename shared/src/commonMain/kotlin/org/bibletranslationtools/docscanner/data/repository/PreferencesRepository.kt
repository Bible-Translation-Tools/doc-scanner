package org.bibletranslationtools.docscanner.data.repository

interface PreferenceRepository {
    /**
     * Returns the value of a user preference
     * @param key
     * @return String
     */
    fun <T>getPref(key: String, type: Class<T>): T?

    /**
     * Returns the value of a user preference or the default value
     */
    fun <T>getPref(key: String, defaultValue: T, type: Class<T>): T

    /**
     * Sets the value of a default preference.
     * @param key
     * @param value if null the string will be removed
     */
    fun <T>setPref(key: String, value: T?, type: Class<T>)
}

inline fun <reified T> PreferenceRepository.getPref(key: String) =
    getPref(key, T::class.java)

inline fun <reified T> PreferenceRepository.getPref(key: String, defaultValue: T) =
    getPref(key, defaultValue, T::class.java)

inline fun <reified T> PreferenceRepository.setPref(key: String, value: T?) =
    setPref(key, value, T::class.java)
