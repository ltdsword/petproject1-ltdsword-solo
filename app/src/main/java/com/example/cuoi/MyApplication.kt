package com.example.cuoi

import android.app.Application
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.AEADBadTagException

class MyApplication : Application() {
    lateinit var sharedPreferences: EncryptedSharedPreferences

    override fun onCreate() {
        super.onCreate()

        try {
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
        } catch (e: AEADBadTagException) {
            // Corrupted, delete and recreate
            deleteSharedPreferences("secure_prefs")

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
        }

        val apiKey = BuildConfig.SENDGRID_API_KEY
        if (!sharedPreferences.contains("API_KEY")) {
            sharedPreferences.edit().putString("API_KEY", apiKey).apply()
        }


        val openAiApiKey = BuildConfig.OPENAI_API_KEY
        if (!sharedPreferences.contains("OPENAI_API_KEY")) {
            sharedPreferences.edit().putString("OPENAI_API_KEY", openAiApiKey).apply()
        }
    }
}
