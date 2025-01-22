package com.example.cuoi

import java.security.MessageDigest

class Hasher {
    fun hash(password: String): String {
        val bytes = password.toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}