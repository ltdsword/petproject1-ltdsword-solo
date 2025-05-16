package com.example.cuoi

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HelpFragment : Fragment() {
    private lateinit var adapter: ChatAdapter
    private var chatHistory : MutableList<ChatMessage> = mutableListOf()
    private lateinit var agent: AssistantAgent

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    private lateinit var username: String
    private lateinit var profile: Profile
    private lateinit var userPrefEditor: SharedPreferences.Editor
    private val profileManagement = ProfileManagement()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
//        val progressText = view.findViewById<TextView>(R.id.progressText)
//        val mainContent = view.findViewById<NestedScrollView>(R.id.mainContent)
//
//        // Initial visibility
//        progressBar.visibility = View.VISIBLE
//        progressText.visibility = View.VISIBLE
//        mainContent.visibility = View.GONE
//
//        val animator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100)
//        animator.duration = 400
//
//        animator.addUpdateListener { animation ->
//            val progress = animation.animatedValue as Int
//            progressText.text = "Loading: $progress%"
//        }
//
//        animator.start()

        // Get the username and load profile
        val data = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userPrefEditor = data.edit()
        username = data.getString("username", null) ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val profileTemp = profileManagement.getProfile(username)
            withContext(Dispatchers.Main) {
                if (profileTemp != null) {
                    profile = profileTemp
                    agent = AssistantAgent(profile)
                    chatHistory = agent.chatHistory.chats
                    chatHistory[0] = ChatMessage("assistant", "Hi, I am an assistant which answers your questions or provides recommendations. Feel free to ask!")
//                    mainContent.visibility = View.VISIBLE
//                    progressText.visibility = View.GONE
//                    progressBar.visibility = View.GONE
                    setupUI(view)
                } else {
                    return@withContext
                }
            }
        }
    }

    fun setupUI(view: View) {
        // Set up RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.chatRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatAdapter(chatHistory, profile)
        recyclerView.adapter = adapter

        // Set up EditText for sending messages
        val messageEditText = view.findViewById<TextInputEditText>(R.id.messageEditText)
        val sendButton = view.findViewById<Button>(R.id.Send)

        sendButton.setOnClickListener {
            val text = messageEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                addMessage(ChatMessage("user", text))
                messageEditText.setText("")
                assistantReply(text)
            }
        }

        val deleteFab: FloatingActionButton = view.findViewById(R.id.deleteChatFab)
        deleteFab.setOnClickListener {
            deleteChat()
            Toast.makeText(requireContext(), "Chat deleted", Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun deleteChat() {
        agent.deleteChat()
        chatHistory.clear()
        chatHistory.add(ChatMessage("assistant", "Hi, I am an assistant which answers your questions or provides recommendations. Feel free to ask!"))
        adapter.notifyDataSetChanged()

    }

    private fun addMessage(message: ChatMessage) {
        chatHistory.add(message)
        syncAgent()
        adapter.notifyItemInserted(chatHistory.size - 1)

        val recyclerView = view?.findViewById<RecyclerView>(R.id.chatRecyclerView)
        recyclerView?.post {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            layoutManager?.scrollToPositionWithOffset(chatHistory.size - 1, 20)
        }
    }
    private fun assistantReply(userText: String) {
        // Step 1: Add typing indicator message
        val typingIndicator = ChatMessage("assistant", "...")
        chatHistory.add(typingIndicator)
        val typingPos = chatHistory.size - 1
        adapter.notifyItemInserted(typingPos)

        // Step 2: Get response from agent
        agent.getResponse(requireContext(), userText) { reply ->
            Handler(Looper.getMainLooper()).postDelayed({
                // Step 3: Remove typing indicator
                chatHistory.removeAt(typingPos)
                adapter.notifyItemRemoved(typingPos)

                // Step 4: Animate reply
                animateAssistantReply(reply)
                syncAgent()
            }, 800) // Shorter delay while showing "typing..."
        }
    }


    private fun animateAssistantReply(fullText: String) {
        val typingMessage = ChatMessage("assistant", "")
        chatHistory.add(typingMessage)
        val position = chatHistory.size - 1
        adapter.notifyItemInserted(position)

        var index = 0
        val handler = Handler(Looper.getMainLooper())
        val delay: Long = 15 // milliseconds per character

        val runnable = object : Runnable {
            override fun run() {
                if (index <= fullText.length) {
                    typingMessage.content = fullText.substring(0, index)
                    adapter.notifyItemChanged(position)
                    index++
                    handler.postDelayed(this, delay)
                }
            }
        }

        handler.post(runnable)
    }

    private fun syncChatHistory() {
        chatHistory = agent.chatHistory.chats
        chatHistory[0] = ChatMessage("assistant", "Hi, I am an assistant which answers your questions or provides recommendations. Feel free to ask!")
    }

    private fun syncAgent() {
        val sysPrompt = ChatMessage("system", agent.systemPrompt)
        agent.chatHistory.chats = chatHistory
        agent.chatHistory.chats[0] = sysPrompt
    }
}

class ChatAdapter(private val messages: List<ChatMessage>, private val profile: Profile) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_USER = 1
        const val VIEW_TYPE_ASSISTANT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].role == "user") VIEW_TYPE_USER else VIEW_TYPE_ASSISTANT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            val view = inflater.inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_assistant, parent, false)
            AssistantViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserViewHolder) {
            holder.textMessage.text = message.content
        } else if (holder is AssistantViewHolder) {
            holder.textMessage.text = message.content
        }
    }

    override fun getItemCount() = messages.size

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMessage: TextView = view.findViewById(R.id.textMessage)
    }

    class AssistantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMessage: TextView = view.findViewById(R.id.textMessage)
    }
}
