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
        holder.txt_aqi.text = item[position].value
        val context = holder.txt_details.context
        if(item[position].type == "co"){
            holder.txt_details.text = context.getString(R.string.txt_co)
            holder.txt_name.text = "ก๊าซคาร์บอนมอนอกไซด์ (CO)"
        }else if(item[position].type == "pm25"){
            holder.txt_details.text = context.getString(R.string.txt_pm_25)
            holder.txt_name.text = "ฝุ่นละอองขนาดไม่เกิน 2.5 ไมครอน (PM2.5)"
        }else if(item[position].type == "pm10"){
            holder.txt_details.text = context.getString(R.string.txt_pm_10)
            holder.txt_name.text = "ฝุ่นละอองขนาดไม่เกิน 10 ไมครอน (PM10)"
        }else if(item[position].type == "o3"){
            holder.txt_details.text = context.getString(R.string.txt_o_3)
            holder.txt_name.text = "ก๊าซโอโซน (O3)"
        }else if(item[position].type == "no2"){
            holder.txt_details.text = context.getString(R.string.txt_no_2)
            holder.txt_name.text = "ก๊าซไนโตรเจนไดออกไซด์ (NO2)"
        }else if(item[position].type == "so2"){
            holder.txt_details.text = context.getString(R.string.txt_so_2)
            holder.txt_name.text = "ก๊าซซัลเฟอร์ไดออกไซด์ (SO2)"
        }else {
            holder.txt_details.text = "ไม่มีคำอธิบาย"
            holder.txt_name.text = item[position].type
        }
    }

}

class ViewHolderAqi(view: View):RecyclerView.ViewHolder(view){
    val txt_name = view.txt_name
    val txt_aqi = view.txt_aqi
    val txt_details = view.txt_details
}