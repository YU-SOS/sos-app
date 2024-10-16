import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.Hospital
import com.example.sos.R

class HospitalAdapter(private val hospitals: List<Hospital>) : RecyclerView.Adapter<HospitalAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hospital_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(hospitals[position])
    }

    override fun getItemCount() = hospitals.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hospitalName: TextView = itemView.findViewById(R.id.hospitalName)
        private val hospitalAddress: TextView = itemView.findViewById(R.id.hospitalAddress)

        fun bind(hospital: Hospital) {
            hospitalName.text = hospital.name
            hospitalAddress.text = hospital.address
        }
    }
}
