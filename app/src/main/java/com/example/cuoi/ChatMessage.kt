package com.example.cuoi

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

data class ChatMessage(
    val role: String, // "user" or "assistant"
    var content: String
)

class ChatHistory {
    var chats = mutableListOf<ChatMessage>()
    fun add(message: ChatMessage) {
        chats.add(message)
    }
    // add user prompt
    fun onUserMessage(userText: String) {
        chats.add(ChatMessage("user", userText))
    }
    // add model response
    fun onAssistantMessage(assistantText: String) {
        chats.add(ChatMessage("assistant", assistantText))
    }
    fun delete() {
        chats.clear()
    }
    fun getChat(): List<ChatMessage> {
        return chats
    }
}

class AssistantAgent(private val profile: Profile) {

    private val client = OkHttpClient()
    val chatHistory = ChatHistory()

    private val apiUrl = "https://ai-ltdsword12414689907855.openai.azure.com/openai/deployments/gpt-4o-mini/chat/completions?api-version=2025-01-01-preview"

     val systemPrompt = """
        You are a helpful assistant. The user is named ${profile.name}.
        The user's phone number is ${profile.phoneNumber}, and their email is ${profile.email}.
        The user has friends: ${profile.friends.joinToString(", ") { it.name }}.
        Their loan (respectively) is as follows: ${profile.friends.joinToString(", ") {it.hist.total.toString()}}
        The user has visited the following places: ${profile.cache.keys.joinToString(", ")}.
        The price (respectively) of these places are: ${profile.cache.values.joinToString(", ")}.
        
        You can use this information to answer the user's questions.

        The user may ask you about:
        - The amount of money that a specific friend owes
        - Suggest a place to eat or drink

        If the user requests a place to eat/drink, you can get a random place from the above place to give recommendations.
        Avoid non-food or non-drink places. Please guarantee to answer in the right way.
        Try your best to answer the question, and please pay attention to the user's preferences.
        Try to make your answer lower than 150 words.
    """.trimIndent()

    init {
        // Add system message only once
        chatHistory.add(ChatMessage("system", systemPrompt))
    }

    fun deleteChat() {
        chatHistory.delete()
        chatHistory.add(ChatMessage("system", systemPrompt))
    }

    fun getResponse(context: Context, userText: String, onComplete: (String) -> Unit) {
        val apiKey = (context.applicationContext as MyApplication).sharedPreferences
            .getString("OPENAI_API_KEY", null) ?: return
        //chatHistory.onUserMessage(userText)

        val messages = org.json.JSONArray()
        chatHistory.getChat().forEach {
            messages.put(JSONObject().apply {
                put("role", it.role)
                put("content", it.content)
            })
        }

        val requestBodyJson = JSONObject().apply {
            put("messages", messages)
            put("max_tokens", 500)
            put("temperature", 0.7)
            put("top_p", 1)
            put("frequency_penalty", 0)
            put("presence_penalty", 0)
        }

        val mediaType = "application/json".toMediaType()
        val body = requestBodyJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .addHeader("api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("AssistantAgent", "API call failed: ${e.message}")
                    onComplete("Sorry, I couldn't connect to the assistant.")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            Log.e("AssistantAgent", "Unexpected code $response")
                            onComplete("Sorry, something went wrong.")
                        } else {
                            val json = JSONObject(response.body!!.string())
                            val reply = json
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")

                            //chatHistory.onAssistantMessage(reply)
                            onComplete(reply)
                        }
                    }
                }
            })
        }
    }
}