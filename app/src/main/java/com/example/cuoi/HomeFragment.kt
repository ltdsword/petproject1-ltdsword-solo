package com.example.cuoi

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.content.Context
import android.os.Build
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListPopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
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
    @SuppressLint("DefaultLocale", "SimpleDateFormat", "SetTextI18n", "DiscouragedPrivateApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = data.getString("username", null)

        val profileManager = ProfileManager(requireContext())
        val profiles = profileManager.loadProfiles(requireContext())
        val profile = profiles[username] ?: return
        val cache = profile.getCache()
        val friends = profile.getFriends()

        // set the greetings and username ///////////////////////////////////////
        val formatter = SimpleDateFormat("HH") // "HH" for 24-hour format, "hh" for 12-hour format
        val currentHour = formatter.format(Date()).toInt()

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

        // DropDownList(Spinner) Configurations ////////////////////////////////

        // get the spinner
        val spinner = view.findViewById<Spinner>(R.id.spinner)

        // convert a mutable map to a string list
        val list = cache.map {(place, price) -> "$place: \$${String.format("%.2f", price)}"}
        val cacheList = mutableListOf("Hôm nay bạn ăng gì??")
        cacheList.addAll(list)
        val prices = cache.map{(place, price) -> price}

        // Custom Array Adapter
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cacheList
        ) {
            // function to hide the placeholder (default string) in the dropdown when user click
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                // Hide the placeholder (position 0) in the dropdown
                return if (position == 0) {
                    val hiddenView = TextView(context)
                    hiddenView.height = 0 // Hide the view by setting its height to 0
                    hiddenView.visibility = View.GONE
                    hiddenView
                } else {
                    super.getDropDownView(position, convertView, parent)
                }
            }

            // function to set the color of the default string
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                if (position == 0) {
                    // Set placeholder text color
                    view.setTextColor(ContextCompat.getColor(context, R.color.grey))
                } else {
                    // Set normal text color
                    view.setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter // attach the adapter to the spinner

        // Adjust the dropdown height using reflection
        spinner.post {
            try {
                val popupField = Spinner::class.java.getDeclaredField("mPopup")
                popupField.isAccessible = true
                val popup = popupField.get(spinner) as? ListPopupWindow
                popup?.height = 300 // Set a custom maximum height for the dropdown
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var priceSelected = -1

        // handle events
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // get the item selected
                val selectedItem = cacheList[position]
                if (position > 0) {
                    priceSelected = prices[position-1]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        val priceBox = view.findViewById<EditText>(R.id.price)
        if (priceSelected != -1) {
            priceBox.setText(priceSelected.toString())
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

    }

}