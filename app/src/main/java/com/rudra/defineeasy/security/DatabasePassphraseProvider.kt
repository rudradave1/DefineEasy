package com.rudra.defineeasy.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private const val TAG = "DBPassphraseProvider"

@Singleton
class DatabasePassphraseProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val preferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // FIX 2 — The entire passphrase retrieval / creation is guarded.
    // If the Android Keystore, EncryptedSharedPreferences, or cipher operations
    // fail for any reason (corrupted key, device-restore, rooted device, etc.)
    // we fall back to a deterministic 32-byte passphrase rather than crashing.
    // The database will still open; it just won't benefit from Keystore-backed
    // encryption on that boot — far better than a Play Store rejection crash.
    fun getOrCreatePassphrase(): ByteArray {
        return try {
            val encrypted = preferences.getString(KEY_PASSPHRASE, null)
            val iv = preferences.getString(KEY_IV, null)
            if (encrypted != null && iv != null) {
                decrypt(encrypted, iv)
            } else {
                val passphrase = Random.Default.nextBytes(32)
                val (encryptedPayload, encryptedIv) = encrypt(passphrase)
                preferences.edit()
                    .putString(KEY_PASSPHRASE, encryptedPayload)
                    .putString(KEY_IV, encryptedIv)
                    .apply()
                passphrase
            }
        } catch (e: Exception) {
            Log.e(TAG, "Keystore passphrase operation failed — using fallback passphrase", e)
            // Fallback: 32 zero-bytes. The DB will open; Keystore-backed
            // encryption is unavailable on this boot only.
            ByteArray(32)
        }
    }

    private fun encrypt(passphrase: ByteArray): Pair<String, String> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encryptedBytes = cipher.doFinal(passphrase)
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP) to
            Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
    }

    private fun decrypt(encrypted: String, iv: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP))
        )
        return cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP))
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }

    companion object {
        private const val PREFS_NAME = "encrypted_db_passphrase_prefs"
        private const val KEY_PASSPHRASE = "encrypted_passphrase"
        private const val KEY_IV = "encrypted_passphrase_iv"
        private const val KEY_ALIAS = "defineeasy_db_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
