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
import android.widget.ImageButton
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

    private var changed = false

    private lateinit var friendAdapter: FriendAdapter
    private lateinit var formattedTime: String
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var priceBox: EditText
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var listView: ListView
    private lateinit var friends: MutableList<Friend>
    private lateinit var cache: MutableMap<String, Int>
    private lateinit var places: List<String>
    private lateinit var prices: List<Int>
    private lateinit var cacheList: List<String>
    private lateinit var checkBox: CheckBox
    private lateinit var profiles: MutableMap<String, Profile>
    private lateinit var profileManager: ProfileManager
    private lateinit var profile: Profile
    private lateinit var username: String

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("DefaultLocale", "SimpleDateFormat", "SetTextI18n", "DiscouragedPrivateApi",
        "ResourceAsColor"
    )
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username = data.getString("username", null) ?: return
        profileManager = ProfileManager(requireContext())
        profiles = profileManager.loadProfiles()
        profile = profiles[username] ?: return

        cache = profile.getCache()
        friends = profile.getFriends()

        // set the greetings and username ///////////////////////////////////////
        val formatter = SimpleDateFormat("HH") // "HH" for 24-hour format, "hh" for 12-hour format
        val currentHour = formatter.format(Date()).toInt()
        val calendar = Calendar.getInstance()
        val formatterDate = SimpleDateFormat("MM-dd HH:mm")
        formattedTime = formatterDate.format(calendar.time)

        val greeting = view.findViewById<TextView>(R.id.greeting)
        val usernameBox = view.findViewById<TextView>(R.id.username)
        usernameBox.text = username
        val curText = greeting.text.toString()
        if (currentHour <= 5 || currentHour >= 18) {
            greeting.text = "$curText evening"
        }
        else if (currentHour in 6..11) {
            greeting.text = "$curText morning"
        }
        else {
            greeting.text = "$curText afternoon"
        }

        // DropDownList(AutoCompleteTextView) Configurations ////////////////////////////////

        // get the actv
        autoCompleteTextView = view.findViewById<AutoCompleteTextView>(R.id.cache_autocomplete)
        priceBox = view.findViewById<EditText>(R.id.price)

        // convert a mutable map to a string list
        cacheList = cache.map {(place, price) -> "$place: ${String.format("%d", price)}"}
        prices = cache.values.toList()
        places = cache.keys.toList()

        // Custom Array Adapter
        adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            cacheList
        )
        autoCompleteTextView.setAdapter(adapter)

        // Set dropdown behavior
        autoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
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
            placeSelected = adapter.getItem(position) ?: ""
            placeSelected = placeSelected.split(":")[0] // remove the price
            autoCompleteTextView.setText(placeSelected)
            priceSelected = cache[placeSelected] ?: 0
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
            priceSelected = priceBox.text.toString().toIntOrNull() ?: 0
        }
        // DropDownList finished ///////////////////////////////////////////////////

        // CheckBox config ////////////////////////////////////////////////////////
        checkBox = view.findViewById<CheckBox>(R.id.checkbox)

        var flag = false;
        if (!checkBox.isChecked) {
            flag = true
        }
        if (flag) {
            checkBox.text = "ok Nhựt"
        }

        // ListView of Friends ////////////////////////////////////////////////////
        for (i in 0 until friends.size) {
            friends[i].syncLevel()
            friends[i].sync()
        }
        listView = view.findViewById(R.id.listView)
        friendAdapter = FriendAdapter(requireContext(), friends, priceBox)
        listView.adapter = friendAdapter

        // set the height of the listView since we don't need it to be scrolled
        setListViewHeight(listView)
        /////////////////

        // Sync the Price when the price box change /////////////////////////////
        priceBox.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                syncPrice(listView, priceBox, "default")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Add Friend ///////////////
        val addFriendButton = view.findViewById<Button>(R.id.addFriendButton)
        addFriendButton.setBackgroundColor(R.color.grey)
        addFriendButton.setOnClickListener {
            addFriend(friendAdapter, listView, friends)
        }

        // Choose All //////////////////////////////////
        val chooseAllButton = view.findViewById<Button>(R.id.chooseAll)
        chooseAllButton.setOnClickListener {
            syncPrice(listView, priceBox, "choose_all")
        }

        // Apply Changes /////////////////////////////////////////////////////////

        val applyButton: Button = view.findViewById(R.id.apply)
        applyButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Double check bro")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes sir") { dialog, _ ->
                    // User confirmed, retrieve prices
                    // Save data
                    applyChanges()
                    dialog.dismiss() // Close the dialog
                }
                .setNegativeButton("No bitc-") { dialog, _ ->
                    // User canceled, just dismiss the dialog
                    dialog.dismiss()
                }
                .show()
        }
    }

    @SuppressLint("DefaultLocale")
    fun applyChanges() {
        val placeSelected = autoCompleteTextView.text.toString()
        val priceSelected = priceBox.text.toString().toIntOrNull() ?: 0
        val priceList = friendAdapter.getData(listView)
        for (i in 0 until priceList.size) {
            if (priceList[i] != 0) {
                friends[i].hist.addObject(formattedTime, placeSelected, priceSelected)
                friends[i].sync()
            }
        }
        if (checkBox.isChecked) {
            if (placeSelected != "" && priceSelected != 0) profile.addCache(placeSelected, priceSelected)
            cacheList = cache.map {(place, price) -> "$place: ${String.format("%d", price)}"}
            prices = cache.values.toList()
            places = cache.keys.toList()

            adapter.clear()
            adapter.addAll(cacheList)
            adapter.notifyDataSetChanged()
        }
        profile.setFriends(friends)
        profiles[username] = profile
        profileManager.saveProfiles(profiles)
        /////////////////////////////
        // Toast.makeText(requireContext(), "Saved 6a6y", Toast.LENGTH_SHORT).show()
        // reset all
        friendAdapter.reset(listView)
        autoCompleteTextView.setText("")
        priceBox.setText("")
        checkBox.isChecked = true

        changed = false
        friendAdapter.changed = false
    }

    // save progress when switch to another fragment ////////////
    override fun onPause() {
        super.onPause()

        if (changed ||
            autoCompleteTextView.text.toString() != "" ||
            priceBox.text.toString() != "" ||
            friendAdapter.changed) {

            AlertDialog.Builder(requireContext())
                .setTitle("Save Progress")
                .setMessage("U want to save?")
                .setPositiveButton("Yes sir") { _, _ ->
                    applyChanges()
                }
                .setNegativeButton("Nooooo") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
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

    private fun addFriend(friendAdapter: FriendAdapter, listView: ListView, friends: MutableList<Friend>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_friend, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.name_input)
        val emailEditText = dialogView.findViewById<EditText>(R.id.email_input)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Friend")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val name = nameEditText.text.toString()
                val email = emailEditText.text.toString()

                if (name.isNotBlank()) {
                    val newFriend = Friend(name, email, "")
                    newFriend.color = R.color.grey
                    Log.d("MyTag", "prev. size: $friends.size")
                    friends.add(newFriend)
                    Log.d("MyTag", "size: $friends.size")

                    // update the friend adapter
//                    friendAdapter.clear()
//                    Log.d("MyTag", "1: $friends.size")
//                    friendAdapter.addAll(friends)
//                    Log.d("MyTag", "2: $friends.size")
                    friendAdapter.notifyDataSetChanged() // Update ListView
                    setListViewHeight(listView)
                    listView.invalidateViews()
                    listView.refreshDrawableState()

                    changed = true
                    Toast.makeText(requireContext(), "Yippee", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Double check again!!!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun syncPrice(listView: ListView, priceBox: EditText, mode: String = "default") {
        for (i in 0 until listView.count) {
            val view = listView.getChildAt(i) ?: continue
            val checkBoxPrice = view.findViewById<CheckBox>(R.id.checkBoxPrice)
            val checkBoxExtra = view.findViewById<CheckBox>(R.id.checkBoxExtra)
            val price = view.findViewById<EditText>(R.id.price)
            val extra = view.findViewById<EditText>(R.id.extra)
            val total = view.findViewById<TextView>(R.id.total)
            if (mode == "choose_all") {
                checkBoxPrice.isChecked = true
                price.text = priceBox.text
            }
            else {
                if (checkBoxPrice.isChecked) {
                    price.text = priceBox.text
                }
                else {
                    price.setText("")
                }
            }
            val p = price.text.toString().toIntOrNull() ?: 0
            var e = extra.text.toString().toIntOrNull() ?: 0
            if (!checkBoxExtra.isChecked) e = 0
            if (price.text.toString() == "") {
                total.text = ""
            }
            else {
                total.text = (p + e).toString()
                if (p + e == 0) {
                    total.text = "0OOO"
                }
            }
        }
    }
}


class FriendAdapter(context: Context, private val friends: MutableList<Friend>, private val priceBox: EditText) :
    ArrayAdapter<Friend>(context, 0, friends) {

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
        val deleteButton = view.findViewById<ImageButton>(R.id.clearFriend)

        nameTextView.text = friend.name
        emailTextView.text = friend.email
        nameTextView.setTextColor(ContextCompat.getColor(context, friends[position].color))

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
                total.setTextColor(ContextCompat.getColor(context, R.color.orange))
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
            changed = true
        }

        checkBoxExtra.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (extra.text.toString() == "") {
                    extra.setText("0000")
                }
            }
            update()
            changed = true
        }

        price.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                update()
            }
            override fun afterTextChanged(s: Editable?) {
                changed = true
            }
        })

        extra.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                update()
            }
            override fun afterTextChanged(s: Editable?) {
                changed = true
            }
        })

        deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Friend")
                .setMessage("Are you sure?")
                .setPositiveButton("Uhm") { _, _ ->
                    // Remove the friend from the list
                    friends.removeAt(position)
                    // Notify the adapter that the data has changed
                    notifyDataSetChanged()
                    setListViewHeight(parent as ListView)
                    changed = true
                }
                .setNegativeButton("Never mind") { dialog, _ ->
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
            val nameTextView = view.findViewById<TextView>(R.id.friend_name)
            checkBoxPrice.isChecked = false
            checkBoxExtra.isChecked = false
            priceEditText.setText("")
            extraEditText.setText("")
            nameTextView.setTextColor(ContextCompat.getColor(context, friends[i].color))
        }
        changed = false
    }
}