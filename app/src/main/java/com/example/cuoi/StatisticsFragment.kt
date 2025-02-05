package com.example.cuoi

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.Gravity
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListPopupWindow
import android.widget.ListView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat

class StatisticsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    private lateinit var username: String
    private lateinit var profileManager: ProfileManager
    private lateinit var profiles: MutableMap<String, Profile>
    private lateinit var profile: Profile
    private lateinit var friends: MutableList<Friend>

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username = data.getString("username", null) ?: return
        profileManager = ProfileManager(requireContext())
        profiles = profileManager.loadProfiles()
        profile = profiles[username] ?: return

        val cache = profile.getCache()
        friends = profile.getFriends()

        val currentTotalMoney = view.findViewById<TextView>(R.id.currentMoney)
        currentTotalMoney.text = profile.getTotal().toString() + " VND"

        for (i in 0 until friends.size) {
            friends[i].syncLevel()
            friends[i].sync()
        }
        val listView: ListView = view.findViewById(R.id.listView)
        val friendAdapter = FriendStatAdapter(requireContext(), friends, view)
        listView.adapter = friendAdapter
    }

    private fun setListViewHeight(listView: ListView) {
        val listViewAdapter = listView.adapter ?: return
        var totalHeight = 0
        for (i in 0 until listViewAdapter.count) {
            val listItem = listViewAdapter.getView(i, null, listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (listViewAdapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }
}


class FriendStatAdapter(context: Context, private val friends: MutableList<Friend>, private val fragmentView: View) :
    ArrayAdapter<Friend>(context, 0, friends) {


    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the current Friend object
        var friend = friends[position]
        val money = friend.hist.getTotal()

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

        Log.d("MyTag", "position: $position, friend.color: ${friend.index}, total: ${friend.hist.getTotal()}")

        nameBox.setTextColor(ContextCompat.getColor(context, friend.color))
        moneyText.setTextColor(ContextCompat.getColor(context, friend.color))

        if (money != 0) {
            paidButton.setBackgroundColor(ContextCompat.getColor(context, R.color.teal_700))
        }


        // Change Info button
        val changeInfoButton = view.findViewById<ImageButton>(R.id.changeButton)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_friend, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.name_input)
        val emailEditText = dialogView.findViewById<EditText>(R.id.email_input)
        nameEditText.setText(friends[position].name)
        emailEditText.setText(friends[position].email)

        changeInfoButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Chỉnh sửa")
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

                        Toast.makeText(context, "Rồi đó", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Check info kĩ vào!!!", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Thôi") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        // Pay button
        paidButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Xác nhận")
                .setMessage("Chắc là trả chưa dậy?")
                .setPositiveButton("Rồi") { _, _ ->
                    if (historyContainer.visibility == View.VISIBLE) {
                        collapse(historyContainer)
                    }
                    friend.hist.clear()
                    friends[position].hist.clear()
                    friends[position].sync()
                    sync(fragmentView.findViewById(R.id.listView))
                    paidButton.setBackgroundColor(ContextCompat.getColor(context, R.color.grey))
                }
                .setNegativeButton("Chưa") { dialog, _ ->
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
                    text = "Không có lịch sử!"
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
        val data = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = data.getString("username", null) ?: return
        val profileManager = ProfileManager(context)
        val profiles = profileManager.loadProfiles()
        val profile = profiles[username] ?: return
        val currentTotalMoney = fragmentView.findViewById<TextView>(R.id.currentMoney)
        currentTotalMoney.text = profile.getTotal().toString() + " VND"

        for (i in 0 until listView.count) {
            val view = listView.getChildAt(i) ?: continue
            val moneyText = view.findViewById<TextView>(R.id.money)
            val nameText = view.findViewById<TextView>(R.id.friend_name)
            val money = friends[i].hist.getTotal()

            friends[i].sync()
            moneyText.text = money.toString()
            nameText.setTextColor(ContextCompat.getColor(context, friends[i].color))
            moneyText.setTextColor(ContextCompat.getColor(context, friends[i].color))
        }

        profile.setFriends(friends)
        profiles[username] = profile
        profileManager.saveProfiles(profiles)
    }
}