package com.example.upbeat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.upbeat.fragments.HabitsFragment
import com.example.upbeat.fragments.HydrationFragment
import com.example.upbeat.fragments.MoodJournalFragment
import com.example.upbeat.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SharedPreferences with default values if first run
        initializeDefaultPreferences()

        // Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup Bottom Navigation
        bottomNav = findViewById(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        // Optional: Hide bottom nav on certain screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.manageHabitsFragment -> {
                    // Hide bottom nav on manage habits screen
                    bottomNav.visibility = android.view.View.GONE
                }
                else -> {
                    // Show bottom nav on main screens
                    bottomNav.visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    private fun initializeDefaultPreferences() {
        val prefs = getSharedPreferences("WellnessAppPrefs", MODE_PRIVATE)

        // Initialize profile data if first run
        if (!prefs.contains("profile_created_date")) {
            prefs.edit()
                .putString("profile_name", "Your Name")
                .putString("profile_email", "your.email@example.com")
                .putLong("profile_created_date", System.currentTimeMillis())
                .putBoolean("notifications_enabled", true)
                .apply()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
