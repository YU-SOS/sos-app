package com.example.sos.ambulance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.res.ParamedicsRes

class ParamedicsAdapter(private val onItemClick: (ParamedicsRes) -> Unit) :
    RecyclerView.Adapter<ParamedicsAdapter.ParamedicsViewHolder>() {

    private val paramedicsList = mutableListOf<ParamedicsRes>()

    fun updateParamedics(newList: List<ParamedicsRes>) {
        paramedicsList.clear()
        paramedicsList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamedicsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.paramedic_item, parent, false)
        return ParamedicsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParamedicsViewHolder, position: Int) {
        val paramedic = paramedicsList[position]
        holder.bind(paramedic, onItemClick)
    }

    override fun getItemCount(): Int = paramedicsList.size

    class ParamedicsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.paramedic_name)
        private val phoneTextView: TextView = itemView.findViewById(R.id.paramedic_phone)

        fun bind(paramedic: ParamedicsRes, onItemClick: (ParamedicsRes) -> Unit) {
            nameTextView.text = paramedic.name
            phoneTextView.text = paramedic.phoneNumber
            itemView.setOnClickListener { onItemClick(paramedic) }
        }
    }
}
