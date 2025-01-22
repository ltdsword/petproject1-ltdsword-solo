package com.example.cuoi
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProfileManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("dataPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save the map of username to Profile
    fun saveProfiles(profiles: MutableMap<String, Profile>) {
        val json = gson.toJson(profiles)
        sharedPreferences.edit().putString("profiles", json).apply()
    }

    // Retrieve the map of username to Profile
    fun loadProfiles(): MutableMap<String, Profile>? {
        val json = sharedPreferences.getString("profiles", null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, Profile>>() {}.type
            gson.fromJson(json, type)
        } else {
            return null
        }
    }
}
