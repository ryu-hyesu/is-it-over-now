package com.example.myapplication

import android.Manifest
import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.myapplication.data.Root
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.network.TicketAPIService
import com.example.myapplication.ui.TicketAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.lang.Thread.sleep
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    lateinit var mainBinding: ActivityMainBinding

    private lateinit var currentLoc: Location
    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        // 퍼미션 체크
        checkPermissions()

        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.ticketMaster_url))
            .addConverterFactory( GsonConverterFactory.create() )
            .build()
        val service = retrofit.create(TicketAPIService::class.java)

        mainBinding.btnLoc.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }

        mainBinding.btnCalendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)

        }
    }

    override fun onPause() {
        super.onPause()
    }

    fun checkPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG,"Permissions are already granted")  // textView에 출력

        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d(TAG,"FINE_LOCATION is granted")

                    checkNotificationPermissions()
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d(TAG,"COARSE_LOCATION is granted")

                    checkNotificationPermissions()
                }

                else -> {
                    Log.d(TAG,"Location permissions are required")
                }
            }
        }

    fun checkNotificationPermissions() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG,"Notification permissions are already granted")
        } else {
            notificationPermissionRequest.launch(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            )
        }
    }

    val notificationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            when {
                permission.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false) -> {
                    Log.d(TAG,"notification is granted")
                }

                else -> {
                    Log.d(TAG,"notification permission is required")
                }
            }
}



}