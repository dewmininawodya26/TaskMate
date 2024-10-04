package com.example.taskapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class TimerActivity : AppCompatActivity() {

    private var timer: CountDownTimer? = null
    private var startTimeInMillis: Long = 600000 // Default to 10 minutes in milliseconds
    private var timeLeftInMillis: Long = startTimeInMillis

    private lateinit var tvCountDown: TextView
    private lateinit var btnStartPause: Button
    private lateinit var btnReset: Button
    private lateinit var btnSetTime: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "TimerPrefs"
    private val KEY_LAST_DURATION = "lastDuration"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Retrieve last used duration
        startTimeInMillis = sharedPreferences.getLong(KEY_LAST_DURATION, 600000)
        timeLeftInMillis = startTimeInMillis

        // Initialize UI elements
        tvCountDown = findViewById(R.id.tvCountDown)
        btnStartPause = findViewById(R.id.btnStartPause)
        btnReset = findViewById(R.id.btnReset)
        btnSetTime = findViewById(R.id.btnSetTime)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set up button listeners
        btnStartPause.setOnClickListener { toggleTimer() }
        btnReset.setOnClickListener { resetTimer() }
        btnSetTime.setOnClickListener { showTimePickerDialog() }

        // Bottom navigation item selection listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Home::class.java))
                    true
                }
                R.id.nav_task -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_timer -> true
                R.id.nav_calender -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    private fun toggleTimer() {
        if (timer == null) {
            startTimer()
            btnStartPause.text = "Pause"
        } else {
            pauseTimer()
            btnStartPause.text = "Start"
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                timer = null
                btnStartPause.text = "Start"
                showNotification("Timer Finished", "Your countdown timer has ended.")
                showPopup("Timer Elapsed", "Your timer has finished.")
                vibratePhone()
            }
        }.start()
    }

    private fun pauseTimer() {
        timer?.cancel()
        timer = null
    }

    private fun resetTimer() {
        timeLeftInMillis = startTimeInMillis
        updateCountDownText()
        pauseTimer()
    }

    private fun updateCountDownText() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = (timeLeftInMillis / 1000 % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvCountDown.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "timer_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelName = "Timer Notifications"
            val channelDescription = "Channel for timer notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_timer) // Ensure this drawable exists
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun showPopup(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500) // Vibrate for 500 milliseconds for older versions
        }
    }

    private fun showTimePickerDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_time_picker, null)
        val hourPicker = view.findViewById<NumberPicker>(R.id.numberPickerHour)
        val minutePicker = view.findViewById<NumberPicker>(R.id.numberPickerMinute)
        val secondPicker = view.findViewById<NumberPicker>(R.id.numberPickerSecond)

        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        secondPicker.minValue = 0
        secondPicker.maxValue = 59

        val dialog = AlertDialog.Builder(this)
            .setTitle("Select Timer Duration")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                val selectedHours = hourPicker.value
                val selectedMinutes = minutePicker.value
                val selectedSeconds = secondPicker.value

                // Convert the selected time to milliseconds
                startTimeInMillis = ((selectedHours * 3600 + selectedMinutes * 60 + selectedSeconds) * 1000).toLong()
                timeLeftInMillis = startTimeInMillis
                updateCountDownText()

                // Save the selected duration in SharedPreferences
                with(sharedPreferences.edit()) {
                    putLong(KEY_LAST_DURATION, startTimeInMillis)
                    apply()
                }
            }
            .setNegativeButton("Cancel", null)

        dialog.show()
    }
}
