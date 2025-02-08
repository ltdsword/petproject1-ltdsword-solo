package com.example.cuoi

import android.app.Application
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class MyApplication : Application() {
    lateinit var sharedPreferences: EncryptedSharedPreferences

    override fun onCreate() {
        super.onCreate()

        // Read API key from BuildConfig
        val apiKey = BuildConfig.SENDGRID_API_KEY

        // Securely store the key
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            this,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences

        if (!sharedPreferences.contains("API_KEY")) {
            sharedPreferences.edit().putString("API_KEY", apiKey).apply()
        }
    }
}
