package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.Calendar
import com.example.myapplication.data.CalendarDao
import com.example.myapplication.data.CalendarDatabase
import com.example.myapplication.databinding.ActivityCalendarBinding
import com.example.myapplication.databinding.ActivityLocationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarActivity : AppCompatActivity() {
    private val TAG = "CalendarActivity"

    private lateinit var calendarDao: CalendarDao

    private lateinit var binding: ActivityCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db: CalendarDatabase = CalendarDatabase.getDatabase(this)
        calendarDao = db.calendarDao()

        setupCalendarListener()
    }

    private fun setupCalendarListener() {
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            handleDateSelection(year, month, dayOfMonth)
        }
    }

    private fun handleDateSelection(year: Int, month: Int, dayOfMonth: Int) {
        val selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
        binding.etText.setText("")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val calendar = calendarDao.getCalendarByDate(selectedDate)
                Log.d(TAG, selectedDate)
                if (calendar != null) {
                    updateEditTextWithCalendarData(calendar)
                    setupSaveButtonListener(calendar)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching calendar for $selectedDate: ${e.message}")
            }
        }
    }

    private fun updateEditTextWithCalendarData(calendar: Calendar) {
        val calendarText = calendar.text
        binding.etText.setText(calendarText)
    }

    private fun setupSaveButtonListener(calendar: Calendar) {
        binding.btnSave.setOnClickListener {
            val calendarText = binding.etText.text.toString()
            updateCalendarText(calendar, calendarText)
        }
    }

    private fun updateCalendarText(calendar: Calendar, newText: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                calendarDao.updateCalendarById(calendar._id, newText)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating calendar: ${e.message}")
            }
        }
    }
}
