package com.example.myapplication.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.Event
import com.example.myapplication.databinding.ListTicketBinding

class TicketAdapter : RecyclerView.Adapter<TicketAdapter.TicketHolder>() {
    var tickets: List<Event>? = null
    private var itemClickListener: OnItemClickListener? = null

    // 인터페이스 정의
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // 외부에서 클릭 리스너를 설정할 수 있는 메서드
    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    override fun getItemCount(): Int {
        return tickets?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketHolder {
        val itemBinding = ListTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: TicketHolder, position: Int) {
        val currentTicket = tickets?.get(position)
        val localDate = currentTicket?.dates?.start?.localDate
        val currentcenue = currentTicket?.embedded?.venues?.get(0)
        val countryName = currentcenue?.country?.name
        val cityName = currentcenue?.city?.name

        holder.itemBinding.tvName.text = currentTicket?.name
        holder.itemBinding.tvDate.text = localDate
        holder.itemBinding.tvCity.text = cityName
        holder.itemBinding.tvCountry.text = countryName

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }
    }

    class TicketHolder(val itemBinding: ListTicketBinding) : RecyclerView.ViewHolder(itemBinding.root)
}