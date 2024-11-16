package com.example.sos.ambulance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.ParamedicsRes
import com.example.sos.R

class ParamedicsAdapter(
    private val paramedicsList: List<ParamedicsRes>,
    private val onClick: (ParamedicsRes) -> Unit
) : RecyclerView.Adapter<ParamedicsAdapter.ParamedicsViewHolder>() {

    inner class ParamedicsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.text_view_paramedic_name)
        private val phoneTextView: TextView = view.findViewById(R.id.text_view_paramedic_phone)

        fun bind(paramedic: ParamedicsRes) {
            nameTextView.text = paramedic.name
            phoneTextView.text = paramedic.phoneNumber
            itemView.setOnClickListener { onClick(paramedic) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamedicsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_paramedic, parent, false)
        return ParamedicsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParamedicsViewHolder, position: Int) {
        holder.bind(paramedicsList[position])
    }

    override fun getItemCount(): Int = paramedicsList.size
}
