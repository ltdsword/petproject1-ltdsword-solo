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
import android.widget.Spinner
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    private lateinit var username: String
    private lateinit var profileManager: ProfileManager
    private lateinit var profiles: MutableMap<String, Profile>
    private lateinit var profile: Profile
    private lateinit var cache: MutableMap<String, Int>
    private lateinit var cacheList: MutableList<Pair<String, Int>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userPrefEditor = data.edit()
        username = data.getString("username", null) ?: return
        profileManager = ProfileManager(requireContext())
        profiles = profileManager.loadProfiles()
        profile = profiles[username] ?: return
        cache = profile.getCache()
        cacheList = cache.toList().toMutableList()

        val loginPref = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val editor = loginPref.edit()
        val password = loginPref.getString("user_$username", null) ?: return
        val hasher = Hasher()

        val newUsernameBox = view.findViewById<EditText>(R.id.newUsername)
        val passwordToChangeUsernameBox = view.findViewById<EditText>(R.id.passwordToChangeUsername)
        val applyToChangeUsernameButton = view.findViewById<Button>(R.id.applyToChangeUsername)

        val passwordBox = view.findViewById<EditText>(R.id.currentPassword)
        val newPasswordBox = view.findViewById<EditText>(R.id.newPassword)
        val applyChangePasswordButton = view.findViewById<Button>(R.id.applyChangePassword)

        applyToChangeUsernameButton.setOnClickListener {
            val newUsername = newUsernameBox.text.toString()
            val passwordToChangeUsername = passwordToChangeUsernameBox.text.toString()

            if (hasher.hash(passwordToChangeUsername) == password && newUsername != "") {
                // save to the login pref
                editor.putString("user_$newUsername", password)
                editor.remove("user_$username")
                editor.apply()
                // save to the user pref
                userPrefEditor.remove("username")
                userPrefEditor.putString("username", newUsername)
                userPrefEditor.apply()
                // clear the text boxes
                newUsernameBox.text.clear()
                passwordToChangeUsernameBox.text.clear()

                profile.name = newUsername
                profiles[newUsername] = profile
                profiles.remove(username)
                profileManager.saveProfiles(profiles)
                username = newUsername
                Toast.makeText(requireContext(), "Username changed successfully", Toast.LENGTH_SHORT).show()
            }
        }

        applyChangePasswordButton.setOnClickListener {
            val currentPassword = passwordBox.text.toString()
            val newPassword = newPasswordBox.text.toString()

            if (currentPassword != "" && hasher.hash(currentPassword) == password) {
                editor.putString("user_$username", hasher.hash(newPassword))
                editor.apply()
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
                cache[place] = price
            }
            cacheList.clear()
            cacheList.addAll(cache.toList().toMutableList())
            profile.setCache(cache)
            profiles[username] = profile
            profileManager.saveProfiles(profiles)
            adapter.notifyDataSetChanged()
            setListViewHeight(listView)

            Toast.makeText(requireContext(), "Cache list applied successfully", Toast.LENGTH_SHORT).show()
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

    var changed = false

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