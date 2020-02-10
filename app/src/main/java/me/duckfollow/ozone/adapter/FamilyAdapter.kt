package me.duckfollow.ozone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.custom_family_adapter.view.*
import me.duckfollow.ozone.R
import me.duckfollow.ozone.model.FamilyModel
import me.duckfollow.ozone.utils.ConvertImagetoBase64

class FamilyAdapter (val item:ArrayList<FamilyModel>) : RecyclerView.Adapter<ViewHolderFamily>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderFamily {
        return ViewHolderFamily(LayoutInflater.from(parent.context).inflate(R.layout.custom_family_adapter, parent, false))
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: ViewHolderFamily, position: Int) {
       try {
           val b = ConvertImagetoBase64().base64ToBitmap(item[position].img_base64)
           holder.img_profile.setImageBitmap(b)
       }catch (e:Exception){

       }
    }
}

class ViewHolderFamily(view: View):RecyclerView.ViewHolder(view){
    val img_profile = view.img_profile
}