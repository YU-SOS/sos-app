package com.example.sos.ambulance

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sos.res.HospitalRes
import com.example.sos.R

class HospitalAdapter(
    private val onItemClick: (hospitalId: String, hospitalName: String) -> Unit
) : RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    private val hospitals = mutableListOf<HospitalRes>()

    fun updateData(newData: List<HospitalRes>) {
        hospitals.clear()
        hospitals.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = hospitals[position]
        holder.bind(hospital)
        holder.itemView.setOnClickListener {
            onItemClick(hospital.id, hospital.name)
        }
    }

    fun addData(newData: List<HospitalRes>) {
        hospitals.addAll(newData)
        notifyDataSetChanged()
    }


    override fun getItemCount() = hospitals.size

    inner class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hospitalImageView: ImageView = itemView.findViewById(R.id.hospital_image)
        private val hospitalNameTextView: TextView = itemView.findViewById(R.id.hospital_name)
        private val hospitalPhoneTextView: TextView = itemView.findViewById(R.id.hospital_phone)

        fun bind(hospital: HospitalRes) {
            // 병원 이름 바인딩
            hospitalNameTextView.text = hospital.name

            // 병원 전화번호 바인딩
            hospitalPhoneTextView.text = hospital.telephoneNumber

            // 병원 이미지 바인딩 (예: Glide 사용)
            if (!hospital.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(hospital.imageUrl) // Firebase 또는 URL 경로를 로드
                    .placeholder(R.drawable.image2) // 기본 이미지
                    .error(R.drawable.image) // 로드 실패 시 이미지
                    .into(hospitalImageView)
            } else {
                hospitalImageView.setImageResource(R.drawable.image) // 이미지 없을 때 기본값
            }
        }
    }

}
