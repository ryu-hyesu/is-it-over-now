package com.example.myapplication

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.data.Root
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.ActivityTicketBinding
import com.example.myapplication.network.TicketAPIService
import com.example.myapplication.ui.TicketAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TicketActivity : AppCompatActivity() {
    val TAG = "TicketActivity"

    lateinit var adapter : TicketAdapter

    lateinit var ticketBinding: ActivityTicketBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ticketBinding = ActivityTicketBinding.inflate(layoutInflater)
        setContentView(ticketBinding.root)

        adapter = TicketAdapter()
        ticketBinding.rvTickets.adapter = adapter
        ticketBinding.rvTickets.layoutManager = LinearLayoutManager(this@TicketActivity)

        val receivedIntent = intent

        val postCode = receivedIntent.getStringExtra("postCode")

        val retrofit = Retrofit.Builder()
            .baseUrl(resources.getString(R.string.ticketMaster_url))
            .addConverterFactory( GsonConverterFactory.create() )
            .build()

        val service = retrofit.create(TicketAPIService::class.java)

        val targetDate = "K8vZ9175Tr0"

        val apiCallback = object : Callback<Root> {
            override fun onResponse(
                call: Call<Root>,
                response: Response<Root>
            ) {
                if (response.isSuccessful) {
                    val root: Root? = response.body()
                    val eventList = root?.embedded?.events
                    adapter.tickets = eventList
                    adapter.notifyDataSetChanged()

                    adapter.setOnItemClickListener(object : TicketAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val clickedEvent = adapter.tickets?.get(position)
                            val intent = Intent(this@TicketActivity, TicketDetailActivity::class.java)
                            intent.putExtra("eventList", ArrayList(eventList))
                            startActivity(intent)
                        }
                    })
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
            resources.getString(R.string.ticketMaster_key),
            targetDate,
            "80",
            postCode.toString(),
            ""
        )

         apiCall.enqueue(apiCallback)

    }
}
