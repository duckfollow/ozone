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

        val context = holder.txt_name.context

        try {
            val aqi = item[position].aqi.toInt()
            if (aqi <= 50) {
                holder.txt_aqi.setTextColor(context.resources.getColor(R.color.green))
                holder.text_api_detail.text = "ดี"
                holder.text_api_detail.setTextColor(context.resources.getColor(R.color.green))
            }else if (aqi>50 && aqi <=100) {
                holder.txt_aqi.setTextColor(context.resources.getColor(R.color.yellow))
                holder.text_api_detail.text = "ปานกลาง"
                holder.text_api_detail.setTextColor(context.resources.getColor(R.color.yellow))
            }else if (aqi > 100 && aqi <= 150) {
                holder.txt_aqi.setTextColor(context.resources.getColor(R.color.orange))
                holder.text_api_detail.text = "ไม่ดีต่อสุขภาพผู้ป่วยภูมิแพ้"
                holder.text_api_detail.setTextColor(context.resources.getColor(R.color.orange))
            }else if (aqi > 150 && aqi < 200) {
                holder.txt_aqi.setTextColor(context.resources.getColor(R.color.red))
                holder.text_api_detail.text = "ไม่ดีต่อสุขภาพ"
                holder.text_api_detail.setTextColor(context.resources.getColor(R.color.red))
            }else if (aqi < 200 && aqi <= 300) {
                holder.txt_aqi.setTextColor(context.resources.getColor(R.color.violet))
                holder.text_api_detail.text = "ไม่ดีต่อสุขภาพมาก"
                holder.text_api_detail.setTextColor(context.resources.getColor(R.color.violet))
            } else {
                holder.txt_aqi.setTextColor(context.resources.getColor(R.color.super_red))
                holder.text_api_detail.text = "อันตราย"
                holder.text_api_detail.setTextColor(context.resources.getColor(R.color.super_red))
            }
        }catch (e:Exception) {

        }
    }
}

class ViewHolderLocation(view:View):RecyclerView.ViewHolder(view){
    val txt_name = view.txt_name
    val txt_aqi = view.txt_aqi
    val text_api_detail = view.text_api_detail
}