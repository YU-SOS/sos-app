import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.sos.ParamedicsRes
import com.example.sos.R

class ParamedicsAdapter(context: Context, paramedicsList: List<ParamedicsRes>) :
    ArrayAdapter<ParamedicsRes>(context, 0, paramedicsList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val paramedic = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_paramedic, parent, false)

        val nameTextView = view.findViewById<TextView>(R.id.text_view_name)
        val phoneTextView = view.findViewById<TextView>(R.id.text_view_phone)

        nameTextView.text = paramedic?.name
        phoneTextView.text = paramedic?.phoneNumber

        return view
    }
}
