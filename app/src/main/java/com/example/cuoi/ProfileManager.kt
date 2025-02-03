package com.example.cuoi
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProfileManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("DataPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private fun defaultProfile(): Profile {
        var profile = Profile()
        profile.name = "admin"
        profile.email = "admin(j97bocon)@gmail.com"

        val defPassword = "j97"
        val hasher = Hasher()

        // save password
        val sharedInfo = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedInfo.edit()
        editor.putString("user_" + profile.name, hasher.hash(defPassword))
        editor.apply()

        return profile
    }

    // Save the map of username to Profile
    fun saveProfiles(profiles: MutableMap<String, Profile>) {
        val json = gson.toJson(profiles)
        sharedPreferences.edit().putString("profiles", json).apply()
    }

    // Retrieve the map of username to Profile
    fun loadProfiles(): MutableMap<String, Profile> {
        val json = sharedPreferences.getString("profiles", null)
        return if (json != null) {
            val type = object : TypeToken<MutableMap<String, Profile>>() {}.type
            gson.fromJson(json, type)
        } else {
            val ret: MutableMap<String, Profile> = mutableMapOf()
            val profile = defaultProfile()
            ret[profile.name] = profile
            saveProfiles(ret)
            return ret
        }
    }
}
