package com.example.myapplication

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.Country
import com.example.myapplication.data.Event
import com.example.myapplication.data.Root
import com.example.myapplication.data.Venue
import com.example.myapplication.databinding.ActivityLocationBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.network.TicketAPIService
import com.example.myapplication.ui.TicketAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 구글 맵에 공연장 위치 표시
class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LatLngList = mutableListOf<LatLng>()
    private val countryList = mutableListOf<String>()
    private val cityList = mutableListOf<String>()
    private val postCodeList = mutableListOf<String>()

    private val mapFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment.getMapAsync(this)

        fetchTicketInformation()
    }

    private fun fetchTicketInformation() {
        val retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.ticketMaster_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(TicketAPIService::class.java)
        val targetDate = "K8vZ9175Tr0"

        val apiCall: Call<Root> = service.getDailyBoxOfficeResult(
            "json",
            getString(R.string.ticketMaster_key),
            targetDate,
            "80",
            "",
            ""
        )

        apiCall.enqueue(object : Callback<Root> {
            override fun onResponse(call: Call<Root>, response: Response<Root>) {
                if (response.isSuccessful) {
                    response.body()?.let { root ->
                        val events: List<Event> = root.embedded.events
                        events.forEach { event ->
                            event.embedded.venues.forEach { venue ->
                                val location = venue.location
                                val latLng = LatLng(location.latitude.toDouble(), location.longitude.toDouble())
                                val city = venue.city.name
                                val country = venue.country.name
                                val postCode = venue.postalCode

                                if (!postCodeList.contains(postCode)) {
                                    cityList.add(city)
                                    LatLngList.add(latLng)
                                    countryList.add(country)
                                    postCodeList.add(postCode)
                                }
                            }
                        }
                        addMarkers()
                    }
                } else {
                    Log.d(TAG, "Unsuccessful Response")
                }
            }

            override fun onFailure(call: Call<Root>, t: Throwable) {
                Log.d(TAG, "OpenAPI Call Failure ${t.message}")
            }
        })
    }

    private fun addMarkers() {
        googleMap.clear()
        for (index in LatLngList.indices) {
            val markerOptions = MarkerOptions()
                .position(LatLngList[index])
                .title(countryList[index])
                .snippet(cityList[index])

            val marker = googleMap.addMarker(markerOptions)
            marker?.showInfoWindow()
            marker?.tag = postCodeList[index]
        }

        val firstLatLng = LatLngList.firstOrNull()
        firstLatLng?.let {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 10f)
            googleMap.moveCamera(cameraUpdate)
        }

        googleMap.setOnInfoWindowClickListener { marker ->
            val intent = Intent(this@LocationActivity, TicketActivity::class.java)
            intent.putExtra("postCode", marker.tag.toString())
            startActivity(intent)
            false
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locCallback)
    }

    private val locRequest = LocationRequest.Builder(10000)
        .setMinUpdateIntervalMillis(5000)
        .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
        .build()

    private val locCallback = object : LocationCallback() {
        override fun onLocationResult(locResult: LocationResult) {
            val currentLoc: Location = locResult.locations[0]
            Log.d(TAG, "위도 : ${currentLoc.latitude}, 경도 : ${currentLoc.altitude}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locRequest,
            locCallback,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d(TAG, location.toString())
            } else {

            }
        }
    }
}