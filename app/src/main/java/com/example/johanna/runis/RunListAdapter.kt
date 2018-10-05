package com.example.johanna.runis

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.johanna.runis.database.entities.Run
import kotlinx.android.synthetic.main.item_run.view.*

class RunListAdapter(context: Context,
                     private val runs: List<Run>?
): BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.item_run, parent, false)

        val thisRun = runs!![position]

        var tv = rowView.tvDate as TextView
        tv.text = thisRun.date

        tv = rowView.tvTime as TextView
        tv.text = thisRun.time.toString()

        tv = rowView.tvKm as TextView
        tv.text = thisRun.km.toString()

        return rowView
    }

    override fun getItem(position: Int): Any {
        return runs!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return runs!!.size
    }

    private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

}