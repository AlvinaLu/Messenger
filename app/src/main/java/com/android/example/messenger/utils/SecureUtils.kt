package com.android.example.messenger.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

internal object SecureUtils {
    fun getSecurePassword(password: String): String? {
        val salt = "goengtpbsrgjtr".toByteArray()
        var generatedPassword: String? = null
        try {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(salt)
            val bytes = md.digest(password.toByteArray())
            val sb = StringBuilder()
            for (i in bytes.indices) {
                sb.append(Integer.toString((bytes[i].toInt() and 0xff) + 0x100, 16).substring(1))
            }
            generatedPassword = sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return generatedPassword
    }
}