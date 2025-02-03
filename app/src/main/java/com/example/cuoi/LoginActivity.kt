package com.example.cuoi

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.cuoi.Hasher
import com.example.cuoi.ProfileManager

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check login state
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            // Redirect to MainActivity if already logged in
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        // check validation
        val sharedInfo = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val profileManager = ProfileManager(this)

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)

//        val inflater = LayoutInflater.from(this)
//        val navHeader = inflater.inflate(R.layout.nav_header, null)
//
//        val emailBox: TextView = navHeader.findViewById<TextView>(R.id.emailBox)
//        val usernameBox: TextView = navHeader.findViewById<TextView>(R.id.usernameBox)
        val hasher = Hasher()

        val registerText = findViewById<TextView>(R.id.textViewRegister)

        registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val username = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val storedPassword = sharedInfo.getString("user_$username", null)

            Log.d("MyTag", storedPassword + " " + hasher.hash(password))

            if (storedPassword != null && hasher.hash(password) == storedPassword) {
                // Save login state
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", true) // Set login state
                editor.putString("username", username)
                editor.apply()

//                // get the profile info
//                val profiles = profileManager.loadProfiles()
//                val profile = profiles[username]
//                if (profile != null) {
//                    emailBox.text = profile.email
//                    usernameBox.text = profile.name
//                }

                // Navigate to the main activity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
