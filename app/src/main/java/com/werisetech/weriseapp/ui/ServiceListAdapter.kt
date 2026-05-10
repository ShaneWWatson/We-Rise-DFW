package com.werisetech.weriseapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.werisetech.weriseapp.R
import com.werisetech.weriseapp.data.Service
import com.werisetech.weriseapp.util.HoursParser

/**
 * Renders a list of `(Service, distance-in-miles)` pairs as cards. Each card
 * shows the open/closed dot, name, faith-based badge, address, distance, and
 * a short blurb. Tapping a card opens the detail screen via [onClick].
 */
class ServiceListAdapter(
    private val onClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceListAdapter.ViewHolder>() {

    private val items = mutableListOf<Pair<Service, Double>>()

    fun submit(newItems: List<Pair<Service, Double>>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (service, miles) = items[position]
        holder.bind(service, miles, onClick)
    }

    /** Per-card view holder. */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val name: TextView = itemView.findViewById(R.id.serviceName)
        private val address: TextView = itemView.findViewById(R.id.serviceAddress)
        private val meta: TextView = itemView.findViewById(R.id.serviceMeta)
        private val blurb: TextView = itemView.findViewById(R.id.serviceBlurb)
        private val statusDot: ImageView = itemView.findViewById(R.id.statusDot)
        private val faithBadge: ImageView = itemView.findViewById(R.id.faithBadge)

        fun bind(service: Service, miles: Double, onClick: (Service) -> Unit) {
            name.text = service.name
            address.text = service.address
            blurb.text = service.blurb
            faithBadge.visibility = if (service.faithBased) View.VISIBLE else View.GONE

            val isOpen = HoursParser.isOpenAt(service.hours)
            statusDot.setImageResource(if (isOpen) R.drawable.ic_dot_open else R.drawable.ic_dot_closed)

            val statusWord = itemView.context.getString(
                if (isOpen) R.string.open_now else R.string.closed
            )
            meta.text = String.format("%.1f mi · %s", miles, statusWord)

            itemView.setOnClickListener { onClick(service) }
        }
    }
}
