package com.example.sos.ambulance

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.databinding.HospitalItemBinding
import com.example.sos.HospitalRes

class HospitalAdapter(
    private val hospitalList: List<HospitalRes>
) : RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    inner class HospitalViewHolder(private val binding: HospitalItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hospital: HospitalRes) {
            binding.hospitalName.text = hospital.name
            binding.hospitalAddress.text = hospital.address

            binding.root.setOnClickListener {
                // HospitalDetailActivity로 이동
                val context: Context = binding.root.context
                val intent = Intent(context, HospitalDetailActivity::class.java).apply {
                    putExtra("HOSPITAL_ID", hospital.id)
                    putExtra("HOSPITAL_NAME", hospital.name)
                    putExtra("HOSPITAL_ADDRESS", hospital.address)
                    putExtra("HOSPITAL_TELEPHONE", hospital.telephoneNumber)
                    putExtra("HOSPITAL_IMAGE_URL", hospital.imageUrl)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val binding = HospitalItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HospitalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        holder.bind(hospitalList[position])
    }

    override fun getItemCount() = hospitalList.size
}
