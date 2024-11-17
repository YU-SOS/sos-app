package com.example.sos.ambulance

import android.util.Log
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
    private var hospitals: List<HospitalRes> = emptyList(),
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    fun updateData(newHospitals: List<HospitalRes>) {
        hospitals = newHospitals
        Log.d("HospitalAdapter", "Updated hospital list: $hospitals")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = hospitals[position]
        Log.d("HospitalAdapter", "Binding hospital: ${hospital.name}, ${hospital.telephoneNumber}")
        holder.bind(hospital, onItemClick)
    }

    override fun getItemCount(): Int = hospitals.size

    class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hospitalImage: ImageView = itemView.findViewById(R.id.hospital_image)
        private val hospitalName: TextView = itemView.findViewById(R.id.hospital_name)
        private val hospitalPhone: TextView = itemView.findViewById(R.id.hospital_phone)

        fun bind(hospital: HospitalRes, onItemClick: (String) -> Unit) {
            hospitalName.text = hospital.name
            hospitalPhone.text = hospital.telephoneNumber

            Glide.with(itemView.context)
                .load(hospital.imageUrl)
                .placeholder(R.drawable.image2)
                .error(R.drawable.image)
                .into(hospitalImage)

            itemView.setOnClickListener { onItemClick(hospital.id) }
        }
    }
}
