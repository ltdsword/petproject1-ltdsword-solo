package com.example.cuoi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameField = findViewById<EditText>(R.id.editTextUsername)
        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val passwordField = findViewById<EditText>(R.id.editTextPassword)
        val registerButton = findViewById<Button>(R.id.buttonRegister)

        val sharedInfo = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val profileManager = ProfileManager(this)
        val profiles = profileManager.loadProfiles()

        registerButton.setOnClickListener {
            val username = usernameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (password.length > 8) {
                    // Save user data (SharedPreferences, Database, etc.)
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()

                    // Save the login data
                    val editor = sharedInfo.edit()
                    val hasher = Hasher()
                    editor.putString("user_$username", hasher.hash(password))
                    editor.putString("user_$email", hasher.hash(password)) // 2 ways to get in
                    editor.apply()

                    Log.d("MyTag", hasher.hash(password))

                    // Save the profile data
                    var newProfile: Profile = Profile()
                    newProfile.name = username
                    newProfile.email = email
                    newProfile.age = 0
                    newProfile.phoneNumber = ""

                    profiles[username] = newProfile
                    profileManager.saveProfiles(profiles)
                    // set detail in the nav_header
//                    val inflater = LayoutInflater.from(this)
//                    val navHeader = inflater.inflate(R.layout.nav_header, null)
//                    val emailBox: TextView = navHeader.findViewById<TextView>(R.id.emailBox)
//                    val usernameBox: TextView = navHeader.findViewById<TextView>(R.id.usernameBox)
//                    emailBox.text = newProfile.email
//                    usernameBox.text = newProfile.name


                    // Go back to LoginActivity
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish() // Close RegisterActivity
                }
                else {
                    Toast.makeText(this, "Password must have more than 8 characters!", Toast.LENGTH_SHORT). show()
                }
            } else {
                Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
