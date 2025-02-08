package com.example.cuoi

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.content.SharedPreferences
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    private lateinit var username: String
    private lateinit var profile: Profile
    private lateinit var cache: MutableMap<String, Int>
    private lateinit var cacheList: MutableList<Pair<String, Int>>
    private lateinit var userPrefEditor: SharedPreferences.Editor
    private val profileManagement = ProfileManagement()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val progressText = view.findViewById<TextView>(R.id.progressText)
        val mainContent = view.findViewById<NestedScrollView>(R.id.mainContent)

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
        userPrefEditor = data.edit()
        username = data.getString("username", null) ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val profileTemp = profileManagement.getProfile(username)
            withContext(Dispatchers.Main) {
                if (profileTemp != null) {
                    profile = profileTemp
                    mainContent.visibility = View.VISIBLE
                    progressText.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    setupUI(view)
                } else {
                    return@withContext
                }
            }
        }
    }

    private fun setupUI(view: View) {
        cache = profile.cache.toMutableMap()
        cacheList = cache.toList().toMutableList()

        val hasher = Hasher()
        val password = profile.password

        val newUsernameBox = view.findViewById<EditText>(R.id.newUsername)
        val passwordToChangeUsernameBox = view.findViewById<EditText>(R.id.passwordToChangeUsername)
        val applyToChangeUsernameButton = view.findViewById<Button>(R.id.applyToChangeUsername)

        val passwordBox = view.findViewById<EditText>(R.id.currentPassword)
        val newPasswordBox = view.findViewById<EditText>(R.id.newPassword)
        val applyChangePasswordButton = view.findViewById<Button>(R.id.applyChangePassword)

        applyToChangeUsernameButton.setOnClickListener {
            val newUsername = newUsernameBox.text.toString()
            val passwordToChangeUsername = passwordToChangeUsernameBox.text.toString()

            profileManagement.isUsernameTaken(newUsername) { exist ->
                if (exist) {
                    Toast.makeText(requireContext(), "Username already exists", Toast.LENGTH_SHORT).show()
                }
                else {
                    if (hasher.hash(passwordToChangeUsername) == password && newUsername != "") {
                        // save to the user pref
                        userPrefEditor.remove("username")
                        userPrefEditor.putString("username", newUsername)
                        userPrefEditor.apply()
                        // clear the text boxes
                        newUsernameBox.text.clear()
                        passwordToChangeUsernameBox.text.clear()
                        profile.name = newUsername
                        profileManagement.saveProfile(profile)
                        profileManagement.deleteUsername(username)
                        username = newUsername
                        Toast.makeText(requireContext(), "Username changed successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        applyChangePasswordButton.setOnClickListener {
            val currentPassword = passwordBox.text.toString()
            val newPassword = newPasswordBox.text.toString()

            if (hasher.hash(currentPassword) == password) {
                profile.password = hasher.hash(newPassword)
                profileManagement.saveProfile(profile)
                passwordBox.text.clear()
                newPasswordBox.text.clear()
                Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
            }
        }

        // listView for cacheList
        val listView = view.findViewById<ListView>(R.id.listView)
        val adapter = CacheAdapter(requireContext(), cacheList, cache)
        listView.adapter = adapter
        setListViewHeight(listView)

        val applyCacheListButton = view.findViewById<Button>(R.id.applyCacheList)
        applyCacheListButton.setOnClickListener {
            for (i in 0 until listView.count) {
                val childView = listView.getChildAt(i) ?: continue

                val place = childView.findViewById<TextView>(R.id.place).text.toString()
                val price = childView.findViewById<TextView>(R.id.price).text.toString().toIntOrNull() ?: 0
                val initPlace = cacheList[i].first
                val initPrice = cacheList[i].second

                if (place != initPlace) {
                    cache[place] = price
                    cache.remove(initPlace)
                }
                else {
                    if (price != initPrice) {
                        cache[place] = price
                    }
                }
            }
            cacheList.clear()
            cacheList.addAll(cache.toList().toMutableList())
            profile.cache = cache.toMap()
            profileManagement.saveProfile(profile)
            adapter.notifyDataSetChanged()
            setListViewHeight(listView)

            Toast.makeText(requireContext(), "Cache list applied successfully", Toast.LENGTH_SHORT).show()
        }

        // other information
        val bankAccountBox = view.findViewById<EditText>(R.id.bankAccount)
        val bankNameBox = view.findViewById<EditText>(R.id.bankName)
        val phoneNumberBox = view.findViewById<EditText>(R.id.phoneNumber)
        val applyOtherInformationButton = view.findViewById<Button>(R.id.applyOtherInformation)

        bankAccountBox.setText(profile.bankAccount)
        bankNameBox.setText(profile.bankName)
        phoneNumberBox.setText(profile.phoneNumber)

        applyOtherInformationButton.setOnClickListener {
            profile.bankAccount = bankAccountBox.text.toString()
            profile.bankName = bankNameBox.text.toString()
            profile.phoneNumber = phoneNumberBox.text.toString()
            profileManagement.saveProfile(profile)
            Toast.makeText(requireContext(), "Other information applied successfully", Toast.LENGTH_SHORT).show()
        }
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

class CacheAdapter(context: Context, private val cacheList: MutableList<Pair<String, Int>>, private val cache: MutableMap<String, Int>) :
    ArrayAdapter<Pair<String, Int>>(context, 0, cacheList) {
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

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Inflate the view if it's not already created
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.cache_list, parent, false)

        val placeBox = view.findViewById<TextView>(R.id.place)
        val priceBox = view.findViewById<TextView>(R.id.price)
        val deleteButton = view.findViewById<ImageButton>(R.id.clearButton)

        placeBox.text = cacheList[position].first
        priceBox.text = cacheList[position].second.toString()

        deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Cache")
                .setMessage("Are you sure?")
                .setMessage("You cannot undo this action.")
                .setPositiveButton("Uhm") { _, _ ->
                    cache.remove(cacheList[position].first)
                    cacheList.removeAt(position)
                    notifyDataSetChanged()
                    Handler(Looper.getMainLooper()).postDelayed({
                        setListViewHeight(parent as ListView)
                    }, 100)
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Never mind") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        return view
    }
}