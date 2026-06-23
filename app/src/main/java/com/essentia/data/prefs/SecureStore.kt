package com.essentia.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encrypted key-value store for secrets. Backed by EncryptedSharedPreferences,
 * which uses the Android Keystore to wrap a master AES-256 key.
 *
 * Use for: API keys, bearer tokens, anything that must not sit in plaintext on disk.
 *
 * On iOS (when we add it), this becomes a `Keychain`-backed implementation behind
 * the same interface.
 */
class SecureStore(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (t: Throwable) {
        // Some devices fail Keystore init (corrupt keystore, OEM bugs). Fall back to
        // an in-memory store so the app doesn't crash; the user can re-enter keys.
        Log.e(TAG, "EncryptedSharedPreferences init failed; using in-memory fallback", t)
        InMemoryPrefs()
    }

    fun putString(key: String, value: String?) {
        prefs.edit().apply {
            if (value == null) remove(key) else putString(key, value)
        }.apply()
    }

    fun getString(key: String, default: String? = null): String? = prefs.getString(key, default)

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private class InMemoryPrefs : SharedPreferences {
        private val data = mutableMapOf<String, Any?>()
        override fun getAll(): MutableMap<String, *> = data
        override fun getString(key: String?, defValue: String?): String? = data[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            @Suppress("UNCHECKED_CAST") (data[key] as? MutableSet<String>) ?: defValues
        override fun getInt(key: String?, defValue: Int): Int = data[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = data[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = data[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = data[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = data.containsKey(key)
        override fun edit(): SharedPreferences.Editor = InMemoryEditor(data)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        private class InMemoryEditor(private val data: MutableMap<String, Any?>) : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private val removals = mutableSetOf<String>()
            override fun putString(key: String, value: String?) = apply { pending[key] = value }
            override fun putStringSet(key: String, values: MutableSet<String>?) = apply { pending[key] = values }
            override fun putInt(key: String, value: Int) = apply { pending[key] = value }
            override fun putLong(key: String, value: Long) = apply { pending[key] = value }
            override fun putFloat(key: String, value: Float) = apply { pending[key] = value }
            override fun putBoolean(key: String, value: Boolean) = apply { pending[key] = value }
            override fun remove(key: String) = apply { removals.add(key) }
            override fun clear() = apply { pending.clear(); removals.addAll(data.keys) }
            override fun commit(): Boolean { apply(); return true }
            override fun apply() {
                removals.forEach { data.remove(it) }
                pending.forEach { (k, v) -> if (v == null) data.remove(k) else data[k] = v }
            }
        }
    }

    companion object {
        private const val TAG = "SecureStore"
        private const val FILE_NAME = "essentia_secure_prefs"
    }
}

object SecureKeys {
    const val OPENAI_COMPATIBLE_API_KEY = "openai_compatible_api_key"
    const val OPENAI_COMPATIBLE_BASE_URL = "openai_compatible_base_url"
    const val OPENAI_COMPATIBLE_MODEL = "openai_compatible_model"
    const val X_BEARER_TOKEN = "x_bearer_token"
}
