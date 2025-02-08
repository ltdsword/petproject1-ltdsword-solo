package com.example.cuoi

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatisticsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    private lateinit var username: String
    private lateinit var profile: Profile
    private var friends: MutableList<Friend> = mutableListOf()

    private val profileManagement = ProfileManagement()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val progressText = view.findViewById<TextView>(R.id.progressText)
        val mainContent = view.findViewById<LinearLayout>(R.id.mainContent)

        progressBar.visibility = View.VISIBLE
        progressText.visibility = View.VISIBLE
        mainContent.visibility = View.GONE

        val animator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100)
        animator.duration = 400

        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            progressText.text = "Loading: $progress%"
        }

        animator.start()

        val data = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username = data.getString("username", null) ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val profileTemp = profileManagement.getProfile(username)
            withContext(Dispatchers.Main) {
                if (profileTemp != null) {
                    profile = profileTemp
                    progressBar.visibility = View.GONE
                    progressText.visibility = View.GONE
                    mainContent.visibility = View.VISIBLE
                    setupUI(view)
                } else {
                    return@withContext
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI(view: View) {
        friends = profile.friends.toMutableList()

        val currentTotalMoney = view.findViewById<TextView>(R.id.currentMoney)
        currentTotalMoney.text = profile.getTotal().toString() + " VND"

        for (i in 0 until friends.size) {
            friends[i].syncLevel()
            friends[i].sync()
        }
        val listView: ListView = view.findViewById(R.id.listView)
        val friendAdapter = FriendStatAdapter(requireContext(), friends, profile, view)
        listView.adapter = friendAdapter
    }
}


class FriendStatAdapter(context: Context, private val friends: MutableList<Friend>, private val profile: Profile, private val fragmentView: View) :
    ArrayAdapter<Friend>(context, 0, friends) {

        private val profileManagement = ProfileManagement()

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the current Friend object
        var friend = friends[position]
        val money = friend.hist.total

        // Inflate the view if it's not already created
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.friend_stat, parent, false)

        val nameBox = view.findViewById<TextView>(R.id.friend_name) ?: return view
        val emailBox = view.findViewById<TextView>(R.id.friend_email) ?: return view
        val histButton = view.findViewById<Button>(R.id.history) ?: return view
        val paidButton = view.findViewById<Button>(R.id.payButton) ?: return view
        val moneyText = view.findViewById<TextView>(R.id.money) ?: return view
        val historyContainer = view.findViewById<LinearLayout>(R.id.historyContainer) ?: return view
        val historyTable = view.findViewById<TableLayout>(R.id.historyTable) ?: return view

        nameBox.text = friend.name
        emailBox.text = friend.email
        moneyText.text = money.toString()

        nameBox.setTextColor(ContextCompat.getColor(context, friend.color))
        moneyText.setTextColor(ContextCompat.getColor(context, friend.color))

        if (money != 0) {
            paidButton.setBackgroundColor(ContextCompat.getColor(context, R.color.teal_700))
        }

        // Change Info button
        val changeInfoButton = view.findViewById<ImageButton>(R.id.changeButton)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_friend, null)
        if (dialogView.parent != null) {
            (dialogView.parent as ViewGroup).removeView(dialogView)
        }
        val nameEditText = dialogView.findViewById<EditText>(R.id.name_input)
        val emailEditText = dialogView.findViewById<EditText>(R.id.email_input)
        nameEditText.setText(friends[position].name)
        emailEditText.setText(friends[position].email)

        changeInfoButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Edit Info")
                .setView(dialogView)
                .setPositiveButton("Oke") { dialog, _ ->
                    val name = nameEditText.text.toString()
                    val email = emailEditText.text.toString()
                    if (name.isNotBlank()) {
                        friends[position].name = name
                        friends[position].email = email
                        friend = friends[position]
                        nameBox.text = friend.name
                        emailBox.text = friend.email
                        notifyDataSetChanged()
                        sync(fragmentView.findViewById(R.id.listView))

                        Toast.makeText(context, "Yippee", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Double check bro!!!", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Later") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        // Pay button
        paidButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Confirm")
                .setMessage("Are you sure your friend paid you?")
                .setPositiveButton("Yeah") { _, _ ->
                    if (historyContainer.visibility == View.VISIBLE) {
                        collapse(historyContainer)
                    }
                    friend.hist.clear()
                    friends[position].hist.clear()
                    friends[position].sync()
                    sync(fragmentView.findViewById(R.id.listView))
                    paidButton.setBackgroundColor(ContextCompat.getColor(context, R.color.grey))
                }
                .setNegativeButton("Uh no") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        // Hist button
        histButton.setOnClickListener {
            if (historyContainer.visibility == View.VISIBLE) {
                collapse(historyContainer)
            } else {
                expand(historyContainer)
                loadHistory(historyTable, friend)
            }
        }

        // notify button
        val notifyButton = view.findViewById<Button>(R.id.notifyFriend)
        if (!friend.verified) {
            notifyButton.text = "Verify"
        }

        notifyButton.setOnClickListener {
            if (friend.verified) {
                if (friend.hist.total == 0) {
                    Toast.makeText(context, "He/She owes you nothing!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val sendNotification = SendNotification()
                sendNotification.sendNotification(context, friend.email, profile, friend)
            }
            else {
                val emailVerify = EmailVerify()
                if (emailVerify.isValidEmail(emailEditText.text.toString())) {
                    emailVerify.showEmailVerificationDialog(context, emailEditText.text.toString()) { isVerified ->
                        if (isVerified) {
                            friend.verified = true
                            notifyButton.text = "Notify"
                            friends[position] = friend
                            sync(fragmentView.findViewById(R.id.listView))
                        }
                    }
                }
                else {
                    Toast.makeText(context, "Invalid email!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // reset friend
        friends[position] = friend
        return view
    }

    private fun expand(view: View) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val targetHeight = view.measuredHeight

        view.layoutParams.height = 0
        view.visibility = View.VISIBLE

        val animator = ValueAnimator.ofInt(0, targetHeight).apply {
            duration = 300 // Animation duration (milliseconds)
            addUpdateListener { animation ->
                view.layoutParams.height = animation.animatedValue as Int
                view.requestLayout()
            }
        }
        animator.start()
    }

    private fun collapse(view: View) {
        val initialHeight = view.measuredHeight

        val animator = ValueAnimator.ofInt(initialHeight, 0).apply {
            duration = 300 // Animation duration (milliseconds)
            addUpdateListener { animation ->
                view.layoutParams.height = animation.animatedValue as Int
                view.requestLayout()
            }
            doOnEnd { view.visibility = View.GONE }
        }
        animator.start()
    }

    @SuppressLint("SetTextI18n")
    private fun loadHistory(historyTable: TableLayout, friend: Friend) {
        historyTable.removeViews(1, historyTable.childCount - 1) // Clear old rows

        val historyList = friend.hist.getList()

        if (historyList.isEmpty()) {
            val emptyRow = TableRow(context).apply {
                val emptyText = TextView(context).apply {
                    text = "Nothing here bro!"
                    setPadding(8, 4, 8, 4)
                    gravity = Gravity.CENTER
                    setTextColor(ContextCompat.getColor(context, android.R.color.black))
                }
                addView(emptyText)
            }
            historyTable.addView(emptyRow)
            return
        }

        for (entry in historyList) {
            val row = TableRow(context)

            val dateText = TextView(context).apply {
                text = entry.date
                setPadding(8, 4, 8, 4)
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
            }

            val placeText = TextView(context).apply {
                text = entry.name
                setPadding(8, 4, 8, 4)
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
            }

            val priceText = TextView(context).apply {
                text = "${entry.price} đ"
                setPadding(8, 4, 8, 4)
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
            }

            row.addView(dateText)
            row.addView(placeText)
            row.addView(priceText)

            historyTable.addView(row)
        }

    }


    @SuppressLint("SetTextI18n")
    private fun sync(listView: ListView) {
        val currentTotalMoney = fragmentView.findViewById<TextView>(R.id.currentMoney)
        currentTotalMoney.text = profile.getTotal().toString() + " VND"

        for (i in 0 until listView.count) {
            val view = listView.getChildAt(i) ?: continue
            val moneyText = view.findViewById<TextView>(R.id.money)
            val nameText = view.findViewById<TextView>(R.id.friend_name)
            val money = friends[i].hist.total

            friends[i].sync()
            moneyText.text = money.toString()
            nameText.setTextColor(ContextCompat.getColor(context, friends[i].color))
            moneyText.setTextColor(ContextCompat.getColor(context, friends[i].color))
        }

        profile.friends = friends.toList()
        profileManagement.saveProfile(profile)
    }
}