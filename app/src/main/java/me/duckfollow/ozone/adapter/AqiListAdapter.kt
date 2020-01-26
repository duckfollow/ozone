package me.duckfollow.ozone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.duckfollow.ozone.R
import me.duckfollow.ozone.model.AqiModel

class AqiListAdapter (val item:ArrayList<AqiModel>) : RecyclerView.Adapter<ViewHolderLocation>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderLocation {
        return ViewHolderLocation(LayoutInflater.from(parent.context).inflate(R.layout.custom_location_list_adapter, parent, false))
    }

    override fun getItemCount(): Int {
       return item.size
    }

    override fun onBindViewHolder(holder: ViewHolderLocation, position: Int) {
        holder.txt_name.text = item[position].type
        holder.txt_aqi.text = item[position].value
    }

}

class ViewHolderAqi(view: View):RecyclerView.ViewHolder(view){

}