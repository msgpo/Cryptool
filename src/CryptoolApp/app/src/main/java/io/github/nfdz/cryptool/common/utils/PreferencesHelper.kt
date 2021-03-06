package io.github.nfdz.cryptool.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import io.github.nfdz.cryptool.services.BallService
import io.github.nfdz.cryptool.views.cipher.CipherContract
import timber.log.Timber


/**
 * This class has the responsability of managing stored data in preferences.
 * TODO: Now it uses SharedPreferences to decouple implementation but it could be interesting
 * use an interface and a dependency injector to provide the storage functionality.
 */
class PreferencesHelper(private val context: Context) {

    /** Preferences Keys */
    companion object {
        private const val PREFS_FILE_NAME = "cryptool.prefs"
        private const val THEME_NIGHT_MODE = "theme_night_mode"
        private const val LAST_TAB_KEY = "last_tab"
        private const val LAST_MODE_KEY = "cipher_last_mode"
        private const val LAST_PASSPHRASE_KEY = "cipher_last_passphrase"
        private const val LAST_PASSPHRASE_LOCKED_KEY = "cipher_last_passphrase_locked_flag"
        private const val LAST_ORIGIN_TEXT_KEY = "cipher_last_origin_text"
        private const val LAST_BALL_POSITION_KEY = "last_ball_position"
        private const val LAST_BALL_GRAVITY_KEY = "last_ball_gravity"
        private const val LAST_HASH_ORIGIN_TEXT_KEY = "hash_hash_origin_text"
        private const val KEYS_LABEL_KEY = "keys_labels"
        private const val KEYS_VALUE_KEY = "keys_values"
        private const val ACCESS_CODE_KEY = "access_code"
    }

    private val crypto = CryptographyHelper()

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        // TODO: Use EncryptedSharedPreferences when sdk min is ok and lib is not alpha
//        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
//        EncryptedSharedPreferences.create(
//            PREFS_FILE_NAME,
//            masterKeyAlias,
//            context,
//            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//        )
    }

    private fun hideSensitiveField(field: String?): String {
        return if (field?.isNotEmpty() == true) {
            try {
                crypto.encrypt(field, CODE)
            } catch (e: Exception) {
                Timber.e(e)
                ""
            }
        } else {
            ""
        }
    }

    private fun exposeSensitiveField(storedField: String?): String {
        return if (storedField?.isNotEmpty() == true) {
            try {
                crypto.decrypt(storedField, CODE)
            } catch (e: Exception) {
                Timber.e(e)
                ""
            }
        } else {
            ""
        }
    }

    fun getThemeNightMode(): Boolean? {
        if (!preferences.contains(THEME_NIGHT_MODE)) return null
        return preferences.getBoolean(THEME_NIGHT_MODE, false)
    }

    fun setThemeNightMode(nightMode: Boolean?) {
        if (nightMode == null) {
            preferences.edit().remove(THEME_NIGHT_MODE).apply()
        } else {
            preferences.edit().putBoolean(THEME_NIGHT_MODE, nightMode).apply()
        }
    }

    fun getLastMode(): CipherContract.ModeFlag {
        val lastModeString: String = preferences.getString(LAST_MODE_KEY, null) ?: ""
        return try {
            CipherContract.ModeFlag.valueOf(lastModeString)
        } catch (e: IllegalArgumentException) {
            CipherContract.DEFAULT_MODE
        }
    }

    fun setLastMode(mode: CipherContract.ModeFlag) {
        preferences.edit().putString(LAST_MODE_KEY, mode.name).apply()
    }

    fun getLastPassphrase(): String {
        return exposeSensitiveField(preferences.getString(LAST_PASSPHRASE_KEY, null))
    }

    fun setLastPassphrase(passphrase: String) {
        preferences.edit().putString(LAST_PASSPHRASE_KEY, hideSensitiveField(passphrase)).apply()
    }

    fun getLastOriginText(): String = preferences.getString(LAST_ORIGIN_TEXT_KEY, null) ?: ""

    fun setLastOriginText(originText: String) {
        preferences.edit().putString(LAST_ORIGIN_TEXT_KEY, originText).apply()
    }

    fun wasLastPassphraseLocked(): Boolean =
        preferences.getBoolean(LAST_PASSPHRASE_LOCKED_KEY, false)

    fun setLastPassphraseLocked(passphraseLocked: Boolean) {
        preferences.edit().putBoolean(LAST_PASSPHRASE_LOCKED_KEY, passphraseLocked).apply()
    }

    fun getLastBallPosition(): Int = preferences.getInt(LAST_BALL_POSITION_KEY, 0)

    fun getLastBallGravity(): Int =
        preferences.getInt(LAST_BALL_GRAVITY_KEY, BallService.DEFAULT_GRAVITY)

    fun setLastBallPosition(position: Int) {
        preferences.edit().putInt(LAST_BALL_POSITION_KEY, position).apply()
    }

    fun setLastBallGravity(gravity: Int) {
        preferences.edit().putInt(LAST_BALL_GRAVITY_KEY, gravity).apply()
    }

    fun getLastHashOriginText(): String =
        preferences.getString(LAST_HASH_ORIGIN_TEXT_KEY, null) ?: ""

    fun setLastHashOriginText(originText: String) {
        preferences.edit().putString(LAST_HASH_ORIGIN_TEXT_KEY, originText).apply()
    }

    fun getLastTab(): Int = preferences.getInt(LAST_TAB_KEY, 0)

    fun setLastTab(tabIndex: Int) {
        preferences.edit().putInt(LAST_TAB_KEY, tabIndex).apply()
    }

    fun getKeysLabel(): Set<String> = preferences.getStringSet(KEYS_LABEL_KEY, null) ?: emptySet()

    fun setKeysLabel(labels: Set<String>) {
        preferences.edit().putStringSet(KEYS_LABEL_KEY, labels).apply()
    }

    fun getKeysValue(): Set<String> {
        val values = preferences.getStringSet(KEYS_VALUE_KEY, null) ?: emptySet()
        return values.map { value -> exposeSensitiveField(value) }.toHashSet()
    }

    fun setKeysValue(values: Set<String>) {
        val hiddenValues = values.map { value -> hideSensitiveField(value) }.toHashSet()
        preferences.edit().putStringSet(KEYS_VALUE_KEY, hiddenValues).apply()
    }

    fun hasCode(): Boolean {
        return preferences.contains(ACCESS_CODE_KEY)
    }

    fun getCode(): String {
        return exposeSensitiveField(preferences.getString(ACCESS_CODE_KEY, null))
    }

    /**
     * This method set clear all stored data and set the new code.
     */
    fun setCode(code: String) {
        preferences.edit().clear().putString(ACCESS_CODE_KEY, hideSensitiveField(code)).apply()
    }

    /**
     * This method delete all stored data.
     */
    fun deleteCode() {
        preferences.edit().clear().apply()
    }

    @SuppressLint("ApplySharedPref")
    fun clearAllSync() {
        preferences.edit().clear().commit()
    }

}


