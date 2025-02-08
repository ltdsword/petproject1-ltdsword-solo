package com.example.cuoi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var profile: Profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check login state
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        // check validation
        val profileManagement = ProfileManagement()

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val hasher = Hasher()

        val registerText = findViewById<TextView>(R.id.textViewRegister)
        registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val username = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            lifecycleScope.launch {
                val profileTemp = profileManagement.getProfile(username)
                if (profileTemp != null) {
                    profile = profileTemp
                    val profilePassword = profile.password
                    if (profilePassword != hasher.hash(password)) {
                        Toast.makeText(this@LoginActivity, "Invalid password!", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    else {
                        Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("isLoggedIn", true) // Set login state
                        editor.putString("username", username)
                        editor.apply()
                        // Navigate to the main activity
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                }
                else {
                    Toast.makeText(this@LoginActivity, "Invalid name!", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }
        }
    }
}
