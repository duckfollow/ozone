package me.duckfollow.ozone.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.custom_graph_view.view.*
import me.duckfollow.ozone.R
import me.duckfollow.ozone.model.GraphViewModel

class GraphViewAdapter (val item:ArrayList<GraphViewModel>) : RecyclerView.Adapter<ViewHolderGraphView>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderGraphView {
        return ViewHolderGraphView(LayoutInflater.from(parent.context).inflate(R.layout.custom_graph_view, parent, false))
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: ViewHolderGraphView, position: Int) {
        holder.view_graph.layoutParams.height = item[position].aqi *2
        holder.view_graph.setBackgroundColor(Color.parseColor(item[position].color))
        var text1 = ""
        if (position > 0) {
            if (item[position - 1].date.replace(" ","").equals(item[position].date.replace(" ",""))) {
                text1 = ""
            } else {
                text1 = item[position].date
            }
            holder.text_date.text = text1
        } else {
            text1 = item[position].date
            holder.text_date.text = text1
        }
    }
}

class ViewHolderGraphView(view: View):RecyclerView.ViewHolder(view){
    val view_graph = view.view_graph
    val text_date = view.text_date
}