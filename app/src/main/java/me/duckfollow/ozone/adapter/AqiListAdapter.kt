package me.duckfollow.ozone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.custom_aqi_list_adapter.view.*
import me.duckfollow.ozone.R
import me.duckfollow.ozone.model.AqiModel

class AqiListAdapter (val item:ArrayList<AqiModel>) : RecyclerView.Adapter<ViewHolderAqi>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAqi {
        return ViewHolderAqi(LayoutInflater.from(parent.context).inflate(R.layout.custom_aqi_list_adapter, parent, false))
    }

    override fun getItemCount(): Int {
       return item.size
    }

    override fun onBindViewHolder(holder: ViewHolderAqi, position: Int) {
        holder.txt_name.text = item[position].type
        holder.txt_aqi.text = item[position].value
    }

}

class ViewHolderAqi(view: View):RecyclerView.ViewHolder(view){
    val txt_name = view.txt_name
    val txt_aqi = view.txt_aqi
}