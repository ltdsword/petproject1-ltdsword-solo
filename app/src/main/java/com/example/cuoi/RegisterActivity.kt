package com.example.cuoi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    private val profileManagement = ProfileManagement()
    private lateinit var profile: Profile
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameField = findViewById<EditText>(R.id.editTextUsername)
        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val passwordField = findViewById<EditText>(R.id.editTextPassword)
        val registerButton = findViewById<Button>(R.id.buttonRegister)


        registerButton.setOnClickListener {
            val username = usernameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {
                // Check if the username is already taken
                if (profileManagement.isUsernameExist(username)) {
                    Toast.makeText(this, "Username is already taken!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                else if (password.length > 8) {
                    val emailVerify = EmailVerify()
                    if (emailVerify.isValidEmail(email)) {
                        // Send the verification
                        emailVerify.showEmailVerificationDialog(this, email) { isVerified ->
                            if (isVerified) {
                                // Save user data (SharedPreferences, Database, etc.)
                                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                val hasher = Hasher()
                                // Save the profile data
                                val newProfile: Profile = Profile()
                                newProfile.name = username
                                newProfile.password = hasher.hash(password)
                                newProfile.email = email
                                newProfile.age = 0
                                newProfile.phoneNumber = ""
                                newProfile.bankAccount = ""
                                newProfile.bankName = ""

                                profileManagement.saveProfile(newProfile)
                                // Go back to LoginActivity
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish() // Close RegisterActivity
                            }
                        }
                    }
                    else {
                        Toast.makeText(this, "Invalid email!", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Toast.makeText(this, "Password must have more than 8 characters!", Toast.LENGTH_SHORT). show()
                }
            } else {
                Toast.makeText(this, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        val loginButton = findViewById<TextView>(R.id.loginButton)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
