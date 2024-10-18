package com.example.sos.ambulance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.retrofit.AuthService
import com.example.sos.retrofit.SearchHospitalResponse
import com.example.sos.token.TokenManager

class HospitalAdapter(
    private val hospitals: List<SearchHospitalResponse>,
    private val apiService: AuthService,
    private val tokenManager: TokenManager
) : RecyclerView.Adapter<HospitalAdapter.ViewHolder>() {

    private val expandedPositions = mutableSetOf<Int>() // 확장된 항목의 위치 저장

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hospital_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hospital = hospitals[position]
        holder.bind(hospital, position)
    }

    override fun getItemCount() = hospitals.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hospitalName: TextView = itemView.findViewById(R.id.hospitalName)
        private val hospitalAddress: TextView = itemView.findViewById(R.id.hospitalAddress)
        private val detailsButton: Button = itemView.findViewById(R.id.detailsButton)
        private val detailsLayout: View = itemView.findViewById(R.id.detailsLayout) // 상세정보 레이아웃

        fun bind(hospital: SearchHospitalResponse, position: Int) {
            hospitalName.text = hospital.name
            hospitalAddress.text = hospital.address

            // 현재 항목이 확장되었는지 여부를 확인
            detailsLayout.visibility = if (expandedPositions.contains(position)) View.VISIBLE else View.GONE

            detailsButton.setOnClickListener {
                if (expandedPositions.contains(position)) {
                    expandedPositions.remove(position)
                    notifyItemChanged(position)
                } else {
                    expandedPositions.add(position)
                    showHospitalDetails(hospital, position)
                }
            }
        }

        // 상세 정보를 레이아웃에 표시
        private fun showHospitalDetails(hospital: SearchHospitalResponse, position: Int) {
            val detailsTextView: TextView = itemView.findViewById(R.id.detailsTextView)
            detailsTextView.text = """
                병원 이름: ${hospital.name}
                주소: ${hospital.address}
                전화번호: ${hospital.telephoneNumber}
                응급실 상태: ${hospital.page?.let { "상태 정보" } ?: "상태 정보 없음"}
            """.trimIndent()

            // 상세 정보 보이기
            detailsLayout.visibility = View.VISIBLE
            notifyItemChanged(position)
        }
    }
}
