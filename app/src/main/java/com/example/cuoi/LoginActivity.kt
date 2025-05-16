package com.example.cuoi

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.browser.customtabs.CustomTabsIntent
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var profile: Profile
    private val profileManagement = ProfileManagement()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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

        val forgetPasswordText = findViewById<TextView>(R.id.forgotPasswordText)
        forgetPasswordText.setOnClickListener {
            forgotPassword(this) { username ->
                if (username == null) {
                    return@forgotPassword
                }
                profileManagement.getProfileHelper(username) { prof ->
                    if (prof == null) {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                        return@getProfileHelper
                    }
                    else {
                        val emailVerify = EmailVerify()
                        emailVerify.showEmailVerificationDialog(this, prof.email) { success ->
                            if (success) {
                                resetPassword(this, prof)
                            }
                            else {
                                Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT).show()
                                return@showEmailVerificationDialog
                            }
                        }
                    }
                }
            }
        }

        val registerText = findViewById<TextView>(R.id.textViewRegister)
        registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val username = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.first() == '\\' || username.first() == '/') {
                Toast.makeText(this, "Invalid username!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username == "" || password == "") {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
                    Toast.makeText(this@LoginActivity, "Invalid username!", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }
        }
    }

    private fun forgotPassword(context: Context, callback: (String?) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Reset your password")

        val view = LayoutInflater.from(context).inflate(R.layout.forgot_password, null)
        val usernameBox = view.findViewById<EditText>(R.id.username)

        builder.setView(view)
        builder.setPositiveButton("Send", null)  // Delay assigning action
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val sendButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            sendButton.setOnClickListener {
                val username = usernameBox.text.toString().trim()
                if (username.isEmpty()) {
                    Toast.makeText(context, "Please enter your username.", Toast.LENGTH_SHORT).show()
                } else {
                    callback(username)
                    Toast.makeText(context, "Checking your email...", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            cancelButton.setOnClickListener {
                callback(null)
                dialog.dismiss()
            }
        }

        dialog.show()
    }


    private fun resetPassword(context: Context, prof: Profile) {
        val hasher = Hasher()
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Reset your password")

        val view = LayoutInflater.from(context).inflate(R.layout.reset_password, null)
        val newPasswordBox = view.findViewById<EditText>(R.id.newPassword)
        val confirmPasswordBox = view.findViewById<EditText>(R.id.confirmPassword)
        builder.setView(view)

        builder.setPositiveButton("OK", null)
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            okButton.setOnClickListener {
                val newPassword = newPasswordBox.text.toString().trim()
                val confirmPassword = confirmPasswordBox.text.toString().trim()
                if (newPassword.isEmpty()) {
                    Toast.makeText(context, "Password cannot be empty.", Toast.LENGTH_SHORT).show()
                } else {
                    if (newPassword != confirmPassword) {
                        Toast.makeText(context, "The confirm password is incorrect.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    prof.password = hasher.hash(newPassword)
                    profileManagement.saveProfile(prof)
                    Toast.makeText(context, "Reset password successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

}
