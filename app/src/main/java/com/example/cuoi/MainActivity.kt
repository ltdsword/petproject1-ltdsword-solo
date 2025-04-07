package com.example.cuoi

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

// Extend on navigation item selected listener
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // declare and initialize store layout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var username: String
    private lateinit var email: String
    private lateinit var profile: Profile

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = FirebaseFirestore.getInstance()
        // Compare the version
        // If the version is different, give the user the option to update
        db.collection("update").document("version").get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val versionValue = document.getString("value") // Get the "value" field
                    val versionName = packageManager.getPackageInfo(packageName, 0).versionName
                    Log.d("Version", "Current version: $versionName, Latest version: $versionValue")
                    if (versionValue != versionName) {
                        AlertDialog.Builder(this)
                            .setTitle("Update notification")
                            .setMessage("The app has a new update.")
                            .setMessage("Do you want to update?")
                            .setPositiveButton("Yes sure") { dialog, _ ->
                                // User confirmed, retrieve prices
                                // Save data
                                val url = "https://github.com/ltdsword/petproject1-ltdsword-solo/releases/tag/v${versionValue}"
                                val customTabsIntent = CustomTabsIntent.Builder().build()
                                customTabsIntent.launchUrl(this, Uri.parse(url))
                                dialog.dismiss() // Close the dialog
                            }
                            .setNegativeButton("Nono") { dialog, _ ->
                                // User canceled, just dismiss the dialog
                                dialog.dismiss()
                            }
                            .show()
                    }
                } else {
                    Log.e("Firestore", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting document", exception)
            }

        // Check if user is logged in
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // get the user's info
        val data = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username = data.getString("username", null) ?: return
        val profileManagement = ProfileManagement()
        lifecycleScope.launch {
            val profileTemp = profileManagement.getProfile(username)
            if (profileTemp != null) {
                profile = profileTemp
            }
            else {
                return@launch
            }
            email = profile.email

            setContentView(R.layout.activity_main)
            drawerLayout = findViewById(R.id.drawer_layout)

//            // initialize the toolbar
//            val toolbar = findViewById<Toolbar>(R.id.toolbar)
//            setSupportActionBar(toolbar)

            val fabMenu: FloatingActionButton = findViewById(R.id.fab_menu) // FAB button

            // Handle FAB click to open/close the navigation drawer
            fabMenu.setOnClickListener {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START)
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }

            // init navigation view
            val navigationView = findViewById<NavigationView>(R.id.nav_view)
            navigationView.setNavigationItemSelectedListener(this@MainActivity)

//            if (savedInstanceState == null) {
//                replaceFragment(HomeFragment())
//                navigationView.setCheckedItem(R.id.nav_home)
//            }

//            // create a toggle
//            val toggle = ActionBarDrawerToggle(this@MainActivity, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
//            drawerLayout.addDrawerListener(toggle)
//            toggle.syncState()

            // set the default fragment
            if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
                navigationView.setCheckedItem(R.id.nav_home)
            }

            val inflater = LayoutInflater.from(this@MainActivity)
            val navHeader = inflater.inflate(R.layout.nav_header, null)
            val usernameBox = navHeader.findViewById<TextView>(R.id.usernameBox)
            val emailBox = navHeader.findViewById<TextView>(R.id.emailBox)
            usernameBox.text = username
            emailBox.text = email
        }
    }

    // on navi item selected
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_home -> replaceFragment(HomeFragment())
            R.id.nav_settings -> replaceFragment(SettingsFragment())
            R.id.nav_info -> replaceFragment(AboutFragment())
            R.id.nav_analytics -> replaceFragment((StatisticsFragment()))
            R.id.nav_logout -> {
                Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show()
                logout()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // create a replace fragment method using fragment transaction
    private fun replaceFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    // handle event when we want to close the navi view (press on the "back" button)
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        // Redirect to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}