package com.example.taskapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.SearchView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var datePicker: DatePicker
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calender) // Ensure the layout reference is correct

        datePicker = findViewById(R.id.datePicker)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        searchView = findViewById(R.id.searchViewDate)

        // Set up navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            Log.d("CalendarActivity", "Navigation item selected: ${item.itemId}")
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    true
                }
                R.id.nav_task -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_timer -> {
                    startActivity(Intent(this, TimerActivity::class.java)) // Timer activity
                    true
                }
                R.id.nav_calender -> true
                else -> false
            }
        }

        // Set up SearchView listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle date selection when user submits a date
                query?.let {
                    updateDatePicker(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: Handle text change if needed
                return false
            }
        })
    }

    private fun updateDatePicker(dateString: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val date = dateFormat.parse(dateString)
            val calendar = Calendar.getInstance().apply { time = date }
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        } catch (e: Exception) {
            Log.e("CalendarActivity", "Invalid date format: $dateString", e)
            // Optionally show a message to the user
        }
    }
}
