package com.example.billsparser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item.view.*

class ItemAdapter(private val itemsList: List<ItemView>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item,
            parent,
            false
        )

        return ItemViewHolder(itemView)
    }

    override fun getItemCount() = itemsList.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = itemsList[position]
        holder.itemName.text = currentItem.itemName
        holder.itemPrice.text = String.format("%.2f", currentItem.itemPrice / 100F)
    }

    class ItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.itemNameTV
        val itemPrice: TextView = itemView.itemPriceTV
    }
}