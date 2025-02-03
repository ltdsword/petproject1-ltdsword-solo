package com.example.cuoi

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListPopupWindow
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("DefaultLocale", "SimpleDateFormat", "SetTextI18n", "DiscouragedPrivateApi",
        "ResourceAsColor"
    )
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = data.getString("username", null)
        val profileManager = ProfileManager(requireContext())
        val profiles = profileManager.loadProfiles()
        val profile = profiles[username] ?: return

        val cache = profile.getCache()
        val friends = profile.getFriends()

        // set the greetings and username ///////////////////////////////////////
        val formatter = SimpleDateFormat("HH") // "HH" for 24-hour format, "hh" for 12-hour format
        val currentHour = formatter.format(Date()).toInt()
        val calendar = Calendar.getInstance()
        val formatterDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val formattedTime = formatterDate.format(calendar.time)

        val greeting = view.findViewById<TextView>(R.id.greeting)
        val usernameBox = view.findViewById<TextView>(R.id.username)
        usernameBox.text = username
        val curText = greeting.text.toString()
        if (currentHour <= 5 || currentHour >= 18) {
            greeting.text = "$curText tối"
        }
        else if (currentHour in 6..11) {
            greeting.text = "$curText sáng"
        }
        else {
            greeting.text = "$curText chiều"
        }

        // DropDownList(AutoCompleteTextView) Configurations ////////////////////////////////

        // get the actv
        val autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.cache_autocomplete)
        val priceBox = view.findViewById<EditText>(R.id.price)

        // convert a mutable map to a string list
        var cacheList = cache.map {(place, price) -> "$place: ${String.format("%d", price)}"}
        var prices = cache.values.toList()
        var places = cache.keys.toList()

        // Custom Array Adapter
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            cacheList
        )
        autoCompleteTextView.setAdapter(adapter)

        // Set dropdown behavior
        autoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && autoCompleteTextView.text.isEmpty()) {
                autoCompleteTextView.showDropDown()
            }
        }
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }

        var priceSelected = -1
        var placeSelected = ""
        //println(autoCompleteTextView.text.toString())

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedCache = cacheList[position]
            priceSelected = prices[position]
            Log.d("MyTag", "1-$priceSelected")
            placeSelected = places[position]
            autoCompleteTextView.setText(places[position], false)

            if (priceSelected != -1) {
                priceBox.setText(priceSelected.toString())
            }

            placeSelected = autoCompleteTextView.text.toString()

            val temp = cache[placeSelected]
            if (temp == null) {
                priceBox.setText("0000")
            }
            else {
                priceBox.setText(temp.toString())
            }

            if (placeSelected == "") {
                priceBox.setText("")
            }

            placeSelected = autoCompleteTextView.text.toString()
            Log.d("MyTag", "2-$priceSelected")
            priceSelected = priceBox.text.toString().toIntOrNull() ?: 0
            Log.d("MyTag", "3-$priceSelected")
        }
        // DropDownList finished ///////////////////////////////////////////////////

        // CheckBox config ////////////////////////////////////////////////////////
        val checkBox = view.findViewById<CheckBox>(R.id.checkbox)

        var flag = false;
        if (!checkBox.isChecked) {
            flag = true
        }
        if (flag) {
            checkBox.text = "ok Nhựt"
        }

        // ListView of Friends ////////////////////////////////////////////////////
        val listView: ListView = view.findViewById(R.id.listView)

        priceSelected = priceBox.text.toString().toIntOrNull() ?: 0
        Log.d("MyTag", "str$priceSelected")
        val friendAdapter = FriendAdapter(requireContext(), friends, priceBox)
        listView.adapter = friendAdapter
        /////////////////

        // Apply Changes /////////////////////////////////////////////////////////
        val applyButton: Button = view.findViewById(R.id.apply)
        applyButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Check lại đê")
                .setMessage("Chắc chưa bé?")
                .setPositiveButton("Yes sir") { dialog, _ ->
                    // User confirmed, retrieve prices
                    // Save data
                    placeSelected = autoCompleteTextView.text.toString()
                    priceSelected = priceBox.text.toString().toIntOrNull() ?: 0
                    Log.d("MyTag", "str$priceSelected")
                    val priceList = friendAdapter.getData(listView)
                    for (i in 0..<priceList.size) {
                        if (priceList[i] != 0) {
                            friends[i].hist.addObject(formattedTime, placeSelected, priceSelected)
                        }
                    }
                    if (checkBox.isChecked) {
                        profile.addCache(placeSelected, priceSelected)
                        cacheList = cache.map {(place, price) -> "$place: ${String.format("%d", price)}"}
                        prices = cache.values.toList()
                        places = cache.keys.toList()

                        adapter.clear()
                        adapter.addAll(cacheList)
                        adapter.notifyDataSetChanged()
                    }
                    profile.setFriends(friends)
                    if (username != null) profiles[username] = profile
                    profileManager.saveProfiles(profiles)

                    /////////////////////////////
                    Toast.makeText(requireContext(), "Lưu rồi nha pé", Toast.LENGTH_SHORT).show()
                    // reset all
                    friendAdapter.reset(listView)
                    autoCompleteTextView.setText("")
                    priceBox.setText("")
                    checkBox.isChecked = true
                    dialog.dismiss() // Close the dialog
                }
                .setNegativeButton("No bitc-") { dialog, _ ->
                    // User canceled, just dismiss the dialog
                    dialog.dismiss()
                }
                .show()
        }

        val addFriendButton = view.findViewById<Button>(R.id.addFriendButton)
        addFriendButton.setBackgroundColor(R.color.grey)
        addFriendButton.setOnClickListener {
            addFriend(friendAdapter, listView)
        }
    }

    private fun addFriend(friendAdapter: FriendAdapter, listView: ListView) {
        val data = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = data.getString("username", null)
        val profileManager = ProfileManager(requireContext())
        val profiles = profileManager.loadProfiles()
        val profile = profiles[username]
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_friend, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.name_input)
        val emailEditText = dialogView.findViewById<EditText>(R.id.email_input)

        if (profile == null) return
        val friends = profile.getFriends()

        AlertDialog.Builder(requireContext())
            .setTitle("Thêm thằng")
            .setView(dialogView)
            .setPositiveButton("Thêm") { dialog, _ ->
                val name = nameEditText.text.toString()
                val email = emailEditText.text.toString()

                if (name.isNotBlank() && email.isNotBlank()) {
                    val newFriend = Friend(name, email, "")
                    friends.add(newFriend)
                    // update the friend adapter
                    friendAdapter.clear()
                    friendAdapter.addAll(friends)
                    friendAdapter.notifyDataSetChanged() // Update ListView
                    listView.invalidateViews()
                    listView.refreshDrawableState()
                    Toast.makeText(requireContext(), "Ồ yea", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Check info kĩ vào!!!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Thôi") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}


class FriendAdapter(context: Context, private val friends: MutableList<Friend>, private val priceBox: EditText) :
    ArrayAdapter<Friend>(context, 0, friends) {

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the current Friend object
        val friend = friends[position]

        // Inflate the view if it's not already created
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.friend_list, parent, false)

        // Bind data to the views
        val nameTextView = view.findViewById<TextView>(R.id.friend_name)
        val emailTextView = view.findViewById<TextView>(R.id.friend_email)
        val checkBoxPrice = view.findViewById<CheckBox>(R.id.checkBoxPrice)
        val checkBoxExtra = view.findViewById<CheckBox>(R.id.checkBoxExtra)
        val price = view.findViewById<EditText>(R.id.price)
        val extra = view.findViewById<EditText>(R.id.extra)
        val total = view.findViewById<TextView>(R.id.total)
        val deleteButton = view.findViewById<Button>(R.id.clearFriend)

        nameTextView.text = friend.name
        emailTextView.text = friend.email

        fun update() {
            val p = price.text.toString().toIntOrNull() ?: 0
            var e = extra.text.toString().toIntOrNull() ?: 0

            if (!checkBoxPrice.isChecked) {
                total.text = ""
                return
            }
            if (!checkBoxExtra.isChecked) e = 0
            if (price.text.toString() == "") {
                total.text = ""
            }
            else {
                total.text = (p + e).toString()
                if (p + e == 0) {
                    total.text = "0OOO"
                }
                total.setTextColor(R.color.redder)
            }
        }

        checkBoxPrice.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val currentPrice = priceBox.text.toString().toIntOrNull() ?: 0
                price.setText(currentPrice.toString()) // Set price dynamically
            } else {
                price.setText("")
            }

            update()
        }

        checkBoxExtra.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (extra.text.toString() == "") {
                    extra.setText("0000")
                }
            }

            update()
        }

        price.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                update()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        extra.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                update()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Xóa bạn bè")
                .setMessage("Chắc chưa bé?")
                .setPositiveButton("Ừa") { _, _ ->
                    // Remove the friend from the list
                    friends.removeAt(position)
                    // Notify the adapter that the data has changed
                    notifyDataSetChanged()
                }
                .setNegativeButton("Thôi khỏi") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }


        return view
    }

    fun getData(listView: ListView): MutableList<Int> {
        val prices = mutableListOf<Int>()
        for (i in 0 until listView.count) {
            val view = listView.getChildAt(i) ?: continue
            val priceEditText = view.findViewById<EditText>(R.id.price)
            val checkBoxPrice = view.findViewById<CheckBox>(R.id.checkBoxPrice)
            val extraEditText = view.findViewById<EditText>(R.id.extra)
            val checkBoxExtra = view.findViewById<CheckBox>(R.id.checkBoxExtra)

            var price = 0
            if (checkBoxPrice.isChecked) {
                val p = priceEditText.text.toString().toIntOrNull() ?: 0
                price += p
            }
            if (checkBoxExtra.isChecked) {
                val extra = extraEditText.text.toString().toIntOrNull() ?: 0
                price += extra
            }
            prices.add(price)
        }
        return prices
    }

    fun reset(listView: ListView) {
        for (i in 0 until listView.count) {
            val view = listView.getChildAt(i) ?: continue
            val priceEditText = view.findViewById<EditText>(R.id.price)
            val extraEditText = view.findViewById<EditText>(R.id.extra)
            val checkBoxPrice = view.findViewById<CheckBox>(R.id.checkBoxPrice)
            val checkBoxExtra = view.findViewById<CheckBox>(R.id.checkBoxExtra)
            checkBoxPrice.isChecked = false
            checkBoxExtra.isChecked = false
            priceEditText.setText("")
            extraEditText.setText("")
        }
    }
}