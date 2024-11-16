package com.example.sos.ambulance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.ParamedicsRes
import com.example.sos.R

// 구급대원 정보를 띄울 때 리사이클러 뷰 사용에 도움을 주는 어댑터
class ParamedicsAdapter(
    private val paramedicsList: List<ParamedicsRes>,
    private val onItemClick: (ParamedicsRes) -> Unit
) : RecyclerView.Adapter<ParamedicsAdapter.ParamedicsViewHolder>() {

    inner class ParamedicsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.text_view_name)
        val phoneTextView: TextView = itemView.findViewById(R.id.text_view_phone)

        fun bind(paramedic: ParamedicsRes) {
            nameTextView.text = paramedic.name
            phoneTextView.text = paramedic.phoneNumber
            itemView.setOnClickListener { onItemClick(paramedic) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParamedicsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_paramedic, parent, false)
        return ParamedicsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParamedicsViewHolder, position: Int) {
        holder.bind(paramedicsList[position])
    }

    override fun getItemCount(): Int = paramedicsList.size
}
