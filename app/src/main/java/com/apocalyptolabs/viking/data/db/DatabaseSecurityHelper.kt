package com.apocalyptolabs.viking.data.db

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object DatabaseSecurityHelper {

    private const val KEY_ALIAS = "viking_sqlcipher_key"
    private const val PREFS_NAME = "viking_sec_prefs"
    private const val PASSPHRASE_KEY = "encrypted_db_passphrase"

    fun getOrGeneratePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingHex = prefs.getString(PASSPHRASE_KEY, null)
        if (existingHex != null) {
            return hexToBytes(existingHex)
        }

        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)
        val newHex = bytesToHex(randomBytes)

        prefs.edit().putString(PASSPHRASE_KEY, newHex).apply()
        return randomBytes
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in result.indices) {
            result[i] = hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return result
    }
}
