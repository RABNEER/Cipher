package com.apocalyptolabs.viking.data.db

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

/**
 * Generates and protects the SQLCipher database passphrase.
 *
 * The 256-bit passphrase is generated once with SecureRandom, then encrypted
 * with an AES-256-GCM key that lives inside the hardware-backed Android
 * Keystore. Only the ciphertext is persisted to preferences, so a stolen
 * backup or rooted data partition cannot reveal the raw database key.
 */
object DatabaseSecurityHelper {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val MASTER_KEY_ALIAS = "viking_db_master_key"
    private const val PREFS_NAME = "viking_sec_prefs"
    private const val PASSPHRASE_KEY = "encrypted_db_passphrase"
    private const val GCM_TAG_LENGTH_BITS = 128

    fun getOrGeneratePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedCiphertext = prefs.getString(PASSPHRASE_KEY, null)
        val masterKey = getOrCreateMasterKey()

        if (storedCiphertext != null) {
            try {
                return decrypt(masterKey, storedCiphertext)
            } catch (_: Exception) {
                // Keystore key rotated/invalidated (e.g. device credential change):
                // fall through and regenerate. Existing encrypted DB becomes
                // unreadable and will be recreated via destructive migration.
            }
        }

        val freshPassphrase = ByteArray(32).also(SecureRandom()::nextBytes)
        val ciphertext = encrypt(masterKey, freshPassphrase)
        prefs.edit().putString(PASSPHRASE_KEY, ciphertext).apply()
        return freshPassphrase
    }

    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(MASTER_KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(key: SecretKey, plain: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plain)
        return Base64.encodeToString(iv + ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(key: SecretKey, encoded: String): ByteArray {
        val blob = Base64.decode(encoded, Base64.NO_WRAP)
        val iv = blob.copyOfRange(0, 12)
        val ciphertext = blob.copyOfRange(12, blob.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return cipher.doFinal(ciphertext)
    }
}
