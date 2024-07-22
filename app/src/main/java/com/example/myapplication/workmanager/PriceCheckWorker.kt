package com.example.myapplication.workmanager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.Event
import com.example.myapplication.data.Root
import com.example.myapplication.network.TicketAPIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PriceCheckWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    val TAG = "PriceCheckWorker"

    override fun doWork(): Result {
        val currentPrice = inputData.getDouble("current_price",0.0)
        val eventId = inputData.getString("ticketId")
        val currentPrice2 = inputData.getDouble("current_price",0.0)

        var minPrice = 0.0

        val retrofit = Retrofit.Builder()
            .baseUrl(context.resources.getString(R.string.ticketMaster_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(TicketAPIService::class.java)

        val targetDate = "K8vZ9175Tr0"

        val apiCallback = object : Callback<Root> {
            override fun onResponse(call: Call<Root>, response: Response<Root>) {
                if (response.isSuccessful) {
                    val root: Root? = response.body()
                    val firstEvent: Event? = root?.embedded?.events?.firstOrNull()

                    firstEvent?.let { event ->
                        val eventId = event.id.toString()
                        val priceRanges = event.priceRanges
                        minPrice = priceRanges.firstOrNull()?.min?.toDoubleOrNull() ?: 0.0
                        Log.d(TAG, currentPrice.toString() + " : " + minPrice)
                        if (currentPrice >= minPrice) {
                            createNotificationChannel()
                            displayNotification()

                            // 알림 받았다면 workmanager에서 제거, test 위해 제거x
                            // WorkManager.getInstance(applicationContext).cancelWorkById(UUID.fromString(eventId))
                        }
                    }
                } else {
                    Log.d(ContentValues.TAG, "Unsuccessful Response")
                }
            }

            override fun onFailure(call: Call<Root>, t: Throwable) {
                Log.d(ContentValues.TAG, "OpenAPI Call Failure ${t.message}")
            }
        }

        val apiCall: Call<Root> = service.getDailyBoxOfficeResult(
            "json",
            context.resources.getString(R.string.ticketMaster_key),
            targetDate,
            "1",
            "",
            eventId.toString()
        )

        apiCall.enqueue(apiCallback)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Test Channel"
            val descriptionText = "Test Channel Message"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(context.resources.getString(R.string.channel_id), name, importance)
            mChannel.description = descriptionText
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun displayNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelID = context.resources.getString(R.string.channel_id)
        val builder = NotificationCompat.Builder(context, channelID).apply {
            setSmallIcon(R.drawable.ic_action_name)
            setContentTitle("테일러 스위프트 콘서트 티켓")
            setContentText("현재 콘서트 티켓이 원하는 가격보다 저렴합니다!")
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(100, builder.build())
    }
}
