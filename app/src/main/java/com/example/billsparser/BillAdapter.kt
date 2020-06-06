package com.example.billsparser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bill.view.*

class BillAdapter(private val billList: List<BillView>) :
    RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.bill,
            parent,
            false
        )
        return BillViewHolder(itemView)
    }

    override fun getItemCount() = billList.size

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val currentItem = billList[position]
        holder.debtorNameView.text = currentItem.debtorName
        holder.debtAmountView.text = String.format("%.2f", currentItem.debtAmount)
    }

    class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val debtorNameView: TextView = itemView.debtorNameTV
        val debtAmountView: TextView = itemView.debtAmountTV
    }

}