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
import android.view.Gravity
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
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
        currentTotalMoney.text = profile.getTotal().toString()

        val listView: ListView = view.findViewById(R.id.listView)
        val friendAdapter = FriendStatAdapter(requireContext(), friends, view)
        listView.adapter = friendAdapter
    }
}


class FriendStatAdapter(context: Context, private val friends: MutableList<Friend>, private val fragmentView: View) :
    ArrayAdapter<Friend>(context, 0, friends) {

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the current Friend object
        val friend = friends[position]
        val money = friend.hist.getTotal()

        // Inflate the view if it's not already created
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.friend_list, parent, false)

        val nameBox = view.findViewById<TextView>(R.id.friend_name)
        val emailBox = view.findViewById<TextView>(R.id.friend_email)
        val histButton = view.findViewById<Button>(R.id.history)
        val paidButton = view.findViewById<Button>(R.id.payButton)
        val moneyText = view.findViewById<TextView>(R.id.money)
        val historyContainer = view.findViewById<LinearLayout>(R.id.historyContainer)
        val historyTable = view.findViewById<TableLayout>(R.id.historyTable)

        nameBox.text = friend.name
        emailBox.text = friend.email
        moneyText.text = money.toString()

        nameBox.setTextColor(ContextCompat.getColor(context, friend.color))
        moneyText.setTextColor(ContextCompat.getColor(context, friend.color))

        if (money != 0) {
            paidButton.setBackgroundColor(ContextCompat.getColor(context, R.color.teal_700))
        }

        // Pay button
        paidButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Xác nhận")
                .setMessage("Chắc là trả chưa dậy?")
                .setPositiveButton("Rồi") { _, _ ->
                    friend.hist.clear()
                    sync(fragmentView.findViewById(R.id.listView))
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
            }

            val placeText = TextView(context).apply {
                text = entry.name
                setPadding(8, 4, 8, 4)
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
            }

            val priceText = TextView(context).apply {
                text = "${entry.price} đ"
                setPadding(8, 4, 8, 4)
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
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
        currentTotalMoney.text = profile.getTotal().toString()

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

        profiles[username] = profile
        profileManager.saveProfiles(profiles)
    }
}