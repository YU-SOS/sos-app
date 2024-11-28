import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sos.R
import com.example.sos.res.ParamedicsRes

class ParamedicsAdapter(
    private val paramedicsList: List<ParamedicsRes>,
    private val onItemClick: (ParamedicsRes) -> Unit
) : RecyclerView.Adapter<ParamedicsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val paramedicName: TextView = view.findViewById(R.id.paramedic_name)
        val paramedicPhone: TextView = view.findViewById(R.id.paramedic_phone)

        fun bind(paramedic: ParamedicsRes) {
            paramedicName.text = paramedic.name
            paramedicPhone.text = paramedic.phoneNumber

            itemView.setOnClickListener { onItemClick(paramedic) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_paramedic, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(paramedicsList[position])
    }

    override fun getItemCount(): Int = paramedicsList.size
}

