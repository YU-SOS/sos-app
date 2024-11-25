package com.example.sos.ambulance

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sos.res.HospitalRes
import com.example.sos.R

class HospitalAdapter(
    private val onItemClick: (hospitalId: String, hospitalName: String) -> Unit
) : RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    private val hospitalList = mutableListOf<HospitalRes>()

    fun updateData(newData: List<HospitalRes>) {
        hospitalList.clear()
        hospitalList.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = hospitalList[position]
        holder.bind(hospital)
    }

    override fun getItemCount(): Int = hospitalList.size

    inner class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hospitalName: TextView = itemView.findViewById(R.id.hospital_name)
        private val hospitalPhone: TextView = itemView.findViewById(R.id.hospital_phone)
        private val statusIndicator: View = itemView.findViewById(R.id.status_indicator)

        fun bind(hospital: HospitalRes) {
            hospitalName.text = hospital.name
            hospitalPhone.text = hospital.telephoneNumber

            // 병원 상태에 따라 원의 색상 변경 및 클릭 이벤트 처리
            if (hospital.emergencyRoomStatus) {
                // 수용 불가 상태
                statusIndicator.setBackgroundResource(R.drawable.red_circle)
                itemView.isEnabled = false // 클릭 차단
                itemView.alpha = 0.5f // 비활성화된 효과로 반투명 처리
            } else {
                // 수용 가능 상태
                statusIndicator.setBackgroundResource(R.drawable.green_circle)
                itemView.isEnabled = true
                itemView.alpha = 1.0f
            }

            // 클릭 이벤트 설정
            itemView.setOnClickListener {
                if (!hospital.emergencyRoomStatus) { // 수용 가능 상태일 때만 클릭 가능
                    onItemClick(hospital.id, hospital.name)
                } else {
                    Toast.makeText(itemView.context, "병원이 현재 수용 불가 상태입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
