package me.duckfollow.ozone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.custom_location_list_adapter.view.*
import me.duckfollow.ozone.R
import me.duckfollow.ozone.model.ListModel

class LocationListAdapter (val item:ArrayList<ListModel>) :RecyclerView.Adapter<ViewHolderLocation>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderLocation {
        return ViewHolderLocation(LayoutInflater.from(parent.context).inflate(R.layout.custom_location_list_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: ViewHolderLocation, position: Int) {
        holder.txt_name.text = item[position].station_name
        holder.txt_aqi.text = item[position].aqi
    }
}

class ViewHolderLocation(view:View):RecyclerView.ViewHolder(view){
    val txt_name = view.txt_name
    val txt_aqi = view.txt_aqi
}