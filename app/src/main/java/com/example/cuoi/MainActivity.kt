package com.example.cuoi

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.TextView
import org.w3c.dom.Text

// Extend on navigation item selected listener
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // declare and initialize store layout
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if user is logged in
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // Redirect to LoginActivity if not logged in
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // get the user's info
        val data = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = data.getString("username", null)
        val profileManager = ProfileManager(this)
        val profiles = profileManager.loadProfiles()
        val profile = profiles[username] ?: return
        val email = profile.email


        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        // initialize the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // init navigation view
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // create a toggle
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // set the default fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            navigationView.setCheckedItem(R.id.nav_home)
        }

        val inflater = LayoutInflater.from(this)
        val navHeader = inflater.inflate(R.layout.nav_header, null)
        val usernameBox = navHeader.findViewById<TextView>(R.id.usernameBox)
        val emailBox = navHeader.findViewById<TextView>(R.id.emailBox)

        usernameBox.text = username
        emailBox.text = email
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
        editor.clear() // Clear all saved data
        editor.apply()

        // Redirect to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}