package com.example.sos.ambulance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sos.HospitalRes
import com.example.sos.R

class HospitalAdapter(
    private val onItemClick: (String) -> Unit // 클릭 시 병원 id를 전달하는 리스너
) : RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    private var hospitals: List<HospitalRes> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = hospitals[position]
        holder.bind(hospital)
        holder.itemView.setOnClickListener {
            onItemClick(hospital.id) // 병원 id를 클릭 리스너에 전달
        }
    }

    override fun getItemCount(): Int = hospitals.size

    fun updateData(newHospitals: List<HospitalRes>) {
        hospitals = newHospitals
        notifyDataSetChanged()
    }

    class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hospitalImage: ImageView = itemView.findViewById(R.id.hospital_image)
        private val hospitalName: TextView = itemView.findViewById(R.id.hospital_name)
        private val hospitalPhone: TextView = itemView.findViewById(R.id.hospital_phone)

        fun bind(hospital: HospitalRes) {
            hospitalName.text = hospital.name
            hospitalPhone.text = hospital.telephoneNumber
            Glide.with(itemView.context)
                .load(hospital.imageUrl)
                .placeholder(R.drawable.image) // 로딩 중
                .error(R.drawable.image2) // 에러 시 이미지
                .into(hospitalImage)
        }
    }
}
