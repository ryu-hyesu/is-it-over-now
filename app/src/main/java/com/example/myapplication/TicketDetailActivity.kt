package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.example.myapplication.workmanager.PriceCheckWorker
import com.example.myapplication.data.Calendar
import com.example.myapplication.data.CalendarDao
import com.example.myapplication.data.CalendarDatabase
import com.example.myapplication.data.Event
import com.example.myapplication.databinding.ActivityTicketdetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class TicketDetailActivity : AppCompatActivity() {
    val TAG = "TicketDetailActivity"

    private lateinit var ticketDetailBinding: ActivityTicketdetailBinding
    private lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ticketDetailBinding = ActivityTicketdetailBinding.inflate(layoutInflater)
        setContentView(ticketDetailBinding.root)

        if (intent.hasExtra("eventList")) {
            val eventList = intent.getSerializableExtra("eventList") as? ArrayList<Event>

            eventList?.firstOrNull()?.let { event ->
                setupEventDetails(event)
                setAlarmButtonClick(event)
                setShareEventButtonClick(event)
            }
        } else {

        }
    }

    private fun setupEventDetails(event: Event) {
        // 이벤트 디테일 정보 설정
        val eventName = event.name
        val seatmapStaticUrl = event.seatmap.staticUrl
        val priceRanges = event.priceRanges.firstOrNull()
        val currency = priceRanges?.currency

        ticketDetailBinding.tvTitle.text = eventName
        Glide.with(this)
            .load(seatmapStaticUrl)
            .into(ticketDetailBinding.ivSeatmap)

        event.embedded.venues.forEach { venue ->
            val city = venue.city.name
            val country = venue.country.name

            ticketDetailBinding.tvCityName.text = city
            ticketDetailBinding.tvCountryName.text = country
        }

        ticketDetailBinding.tvMin.text = "최저가 : " + priceRanges?.min + currency
        ticketDetailBinding.tvMax.text = "최고가 : " + priceRanges?.max + currency
    }

    private fun setAlarmButtonClick(event: Event) {
        ticketDetailBinding.btnAlarm.setOnClickListener {
            val priceText = ticketDetailBinding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
            // Log.d(TAG, priceText)
            val sendData1 = workDataOf("current_price" to priceText)
            val sendData2 = workDataOf("ticketId" to event.id)
            val sendData3 = workDataOf("current_price2" to priceText)

            val inputData = Data.Builder()
                .putDouble("current_price", priceText)
                .putString("ticketId", event.id)
                .build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWorkRequest = PeriodicWorkRequest
                .Builder(PriceCheckWorker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            workManager = WorkManager.getInstance(applicationContext)
            workManager.enqueueUniquePeriodicWork(event.id.toString(), ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest)

            event?.dates?.start?.localDate?.let { localDate ->
                setSaveCalendarData(this, event.id, localDate, event.name ?: "")
            }
        }
    }

    private fun setShareEventButtonClick(event: Event) {
        var city : String = ""
        var country : String = ""

        event.embedded.venues.forEach { venue ->
            city = venue.city.name
            country = venue.country.name
        }

        val sharedText = event.name + "투어가 " + country + "의 " + city + "에서 열립니다!"

        ticketDetailBinding.btnShare.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, sharedText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }


    fun setSaveCalendarData(context: Context, id: String, date: String, text: String) {

        val db: CalendarDatabase = CalendarDatabase.getDatabase(context)
        val calendarDao: CalendarDao = db.calendarDao()

        val newCalendar = Calendar(_id = id, date = date, text = text)

        CoroutineScope(Dispatchers.IO).launch {
            calendarDao.insertCalendar(newCalendar)
        }
    }

    private fun convertStringToDate(dateString: String): Date {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return format.parse(dateString) ?: Date()
    }

    @SuppressLint("RestrictedApi")
    inline fun workDataOf(vararg pairs: Pair<String, Any?>): Data {
        val dataBuilder = Data.Builder()
        pairs.forEach { pair ->
            dataBuilder.put(pair.first, pair.second)
        }
        return dataBuilder.build()
    }
}
