package com.example.cuoi
import android.app.AlertDialog
import android.content.Context
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Timestamp
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class EmailVerify {
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun generateVerificationCode(): String {
        val random = (100000..999999).random()  // Generates a 6-digit random number
        return random.toString()
    }

    private fun sendVerificationEmail(toEmail: String, verificationCode: String, context: Context) {
        val apiKey = (context.applicationContext as MyApplication).sharedPreferences
            .getString("API_KEY", null) ?: return

        val fromEmail = "ltdsword12@gmail.com"
        val emailBody = """
        <h4>Hello,</h4>
        <p>Below is your verification code:</p>
        <h2>$verificationCode</h2>
        <p>Please enter this code in the app to verify your email.</p>
        <br>
        <p>Best Regards,</p>
        <p><strong>Le Tien Dat</strong>, Developer of the System.</p>
    """.trimIndent()

        val jsonObject = JSONObject().apply {
            put("personalizations", JSONArray().put(JSONObject().apply {
                put("to", JSONArray().put(JSONObject().put("email", toEmail)))
                put("subject", "Verify Your Email")
            }))
            put("from", JSONObject().put("email", fromEmail))
            put("content", JSONArray().put(JSONObject().apply {
                put("type", "text/html")
                put("value", "Your verification code is $verificationCode.")
            }))
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.sendgrid.com/v3/mail/send")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: "No response body"
            }
        })
    }


    fun showEmailVerificationDialog(context: Context, toEmail: String, onSuccess: (Boolean) -> Unit) {
        var correctCode = generateVerificationCode()
        sendVerificationEmail(toEmail, correctCode, context)

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Verify Your Email")

        val view = LayoutInflater.from(context).inflate(R.layout.email_verification, null)
        val codeInput = view.findViewById<EditText>(R.id.codeInput)
        val resendEmailButton = view.findViewById<TextView>(R.id.resendEmailButton)

        builder.setView(view)

        resendEmailButton.setOnClickListener {
            correctCode = generateVerificationCode()
            sendVerificationEmail(toEmail, correctCode, context)
        }

        builder.setPositiveButton("Verify") { _, _ ->
            val enteredCode = codeInput.text.toString()

            if (enteredCode == correctCode) {
                Toast.makeText(context, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                onSuccess(true)
            } else {
                Toast.makeText(context, "Incorrect code. Try again.", Toast.LENGTH_SHORT).show()
                onSuccess(false)
            }
        }

        builder.setNegativeButton("Cancel") { _, _ ->
            onSuccess(false)
        }
        builder.show()
    }
}

class SendNotification {
    fun sendNotification(context: Context, toEmail: String, profile: Profile, friend: Friend) {
        val now = Timestamp.now().seconds
        if (now - friend.lastSent < 86400) {
            Toast.makeText(context, "You need to wait ${((friend.lastSent + 86400 - now)/60).toInt()} minutes before sending.", Toast.LENGTH_SHORT).show()
            return
        }
        friend.lastSent = now
        val sharedPreferences = (context.applicationContext as MyApplication).sharedPreferences
        val apiKey = sharedPreferences.getString("API_KEY", null) ?: return
        val fromEmail = "ltdsword12@gmail.com"

        val emailBody = """
        <h3>Hello,</h3>
        <p>${profile.name} announces to you that:</p>
        <h2>It's time to pay his/her money!!!!!</h2>
        <p>The amount of money is <strong>${friend.hist.total}</strong>.</p>
        <p>You can send it to him/her using the following bank account:</p>
        <p><strong>Bank: ${profile.bankName}</strong></p>
        <p><strong>Account Number: ${profile.bankAccount}</strong></p>
        <p><strong>Phone Number: ${profile.phoneNumber}</strong></p>
        <br>
        <p>Best Regards,</p>
        <p><strong>Le Tien Dat</strong>, Developer of the System.</p>
    """.trimIndent()

        val jsonObject = JSONObject().apply {
            put("personalizations", JSONArray().put(JSONObject().apply {
                put("to", JSONArray().put(JSONObject().put("email", toEmail)))
                put("subject", "Payment Remainder")
            }))
            put("from", JSONObject().put("email", fromEmail))
            put("content", JSONArray().put(JSONObject().apply {
                put("type", "text/html")
                put("value", emailBody.replace("\"", "\\\""))
            }))
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.sendgrid.com/v3/mail/send")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                println("Response Status Code: ${response.code}")
                if (response.code == 202) {
                    Toast.makeText(context, "Send notify email successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}

