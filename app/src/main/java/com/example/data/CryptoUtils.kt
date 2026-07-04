package com.example.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    
    // Derived key for demonstration of robust end-to-end local encryption
    private val secretKeySpec: SecretKeySpec by lazy {
        val keyBytes = "MalawiSchoolSecureKey2026!!!".toByteArray(Charsets.UTF_8)
        // Trim or pad to exactly 32 bytes for AES-256
        val targetBytes = ByteArray(32)
        System.arraycopy(keyBytes, 0, targetBytes, 0, keyBytes.size.coerceAtMost(32))
        SecretKeySpec(targetBytes, "AES")
    }

    /**
     * Encrypts the cleartext using AES-256-GCM.
     * Returns a Base64 encoded string consisting of IV + Ciphertext.
     */
    fun encrypt(cleartext: String): String {
        return try {
            val iv = ByteArray(IV_LENGTH_BYTE)
            SecureRandom().nextBytes(iv)
            
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmSpec)
            
            val ciphertextBytes = cipher.doFinal(cleartext.toByteArray(Charsets.UTF_8))
            
            // Format: iv_base64.ciphertext_base64
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val ciphertextBase64 = Base64.encodeToString(ciphertextBytes, Base64.NO_WRAP)
            "$ivBase64.$ciphertextBase64"
        } catch (e: Exception) {
            "ENCRYPTION_ERROR:${e.localizedMessage}"
        }
    }

    /**
     * Decrypts an AES-256-GCM encoded string (IV + Ciphertext in Base64).
     */
    fun decrypt(encryptedString: String): String {
        if (!encryptedString.contains(".")) {
            return "Decryption failed: Invalid format"
        }
        return try {
            val parts = encryptedString.split(".")
            val ivBytes = Base64.decode(parts[0], Base64.NO_WRAP)
            val ciphertextBytes = Base64.decode(parts[1], Base64.NO_WRAP)
            
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(TAG_LENGTH_BIT, ivBytes)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmSpec)
            
            val decryptedBytes = cipher.doFinal(ciphertextBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "Decryption failed: ${e.localizedMessage}"
        }
    }
}
