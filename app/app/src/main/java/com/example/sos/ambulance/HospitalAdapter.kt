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

class HospitalAdapter(private var hospitalList: List<HospitalRes>, private val onItemClick: (String) -> Unit) : RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    // ViewHolder 정의
    inner class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hospitalName: TextView = itemView.findViewById(R.id.hospital_name)
        val hospitalPhone: TextView = itemView.findViewById(R.id.hospital_phone)
        val hospitalImage: ImageView = itemView.findViewById(R.id.hospital_image)
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(itemView)
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = hospitalList[position]
        holder.hospitalName.text = hospital.name
        holder.hospitalPhone.text = hospital.telephoneNumber

        // 이미지 로딩, 예를 들어 Glide나 Picasso 사용
        Glide.with(holder.itemView.context)
            .load(hospital.imageUrl)  // hospital.imageUrl이 이미지 URL이라고 가정
            .into(holder.hospitalImage)

        // 아이템 클릭 리스너 설정
        holder.itemView.setOnClickListener {
            onItemClick(hospital.id) // hospital.id를 사용하여 병원 ID 전달
        }
    }

    override fun getItemCount(): Int {
        return hospitalList.size
    }

    // 데이터 갱신
    fun updateData(newHospitalList: List<HospitalRes>) {
        hospitalList = newHospitalList
        notifyDataSetChanged()
    }
}
