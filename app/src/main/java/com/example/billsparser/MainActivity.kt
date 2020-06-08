package com.example.billsparser

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item.view.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private val pickJsonFileCode = 111
    private val namesCount = 3
    private val names = arrayOf("Максим", "Заффар", "Никита")
    private val prices = ArrayList<Int>()
    private var totalPrice = 0
    private var totalAmount = Array(namesCount) { 0.toDouble() }
    private val itemsList = ArrayList<ItemView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadButton.setOnClickListener {
            totalAmount = Array(namesCount) { 0.toDouble() }
            itemsList.clear()
            prices.clear()

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }

            startActivityForResult(intent, pickJsonFileCode)
        }
        val debtList = createDebtList()
        debtBox.adapter = BillAdapter(debtList)
        debtBox.layoutManager = LinearLayoutManager(this)
        debtBox.setHasFixedSize(true)
    }

    private fun createDebtList(): List<BillView> {
        val list = arrayListOf<BillView>(
            BillView(
                "Total:",
                totalPrice.toDouble() / 100
            )
        )
        for (i in 0 until namesCount) {
            list.add(BillView(names[i], totalAmount[i] / 100))
        }
        return list
    }

    private fun handleObject(item: JSONObject) {
        val price = item.get("sum") as Int
        prices.add(price)
        val name = item.get("name").toString()
        val spaceInd = name.indexOf(' ')
        itemsList.add(ItemView(name.substring(spaceInd + 1), price))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == pickJsonFileCode && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                val jsonString = readTextFromUri(uri)
                val obj = JSONObject(jsonString)
                totalPrice = obj.get("totalSum") as Int
                val items = obj.getJSONArray("items")
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    handleObject(item)
                }
                itemsBox.adapter = ItemAdapter(itemsList)
                itemsBox.layoutManager = LinearLayoutManager(this)
                itemsBox.setHasFixedSize(true)
                debtBox.adapter = BillAdapter(createDebtList())
            }
        }
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }


    inner class ItemView(val itemName: String, val itemPrice: Int) {
        val indicesOfSelectedNames = ArrayList<Int>()
    }

    inner class ItemAdapter(private val itemsList: List<ItemView>) :
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
            val price = currentItem.itemPrice
            holder.itemName.text = currentItem.itemName
            holder.itemPrice.text = String.format("%.2f ₽", price / 100F)
            holder.namesBox.children.forEachIndexed { index, view ->
                val chk = view as CheckBox
                chk.isChecked = currentItem.indicesOfSelectedNames.contains(index)
                chk.setOnClickListener {
                    var cnt = 0
                    holder.namesBox.children.forEach {
                        if ((it as CheckBox).isChecked) {
                            cnt++
                        }
                    }
                    if (chk.isChecked) {
                        holder.namesBox.children.forEachIndexed { i, chk ->
                            if ((chk as CheckBox).isChecked) {
                                totalAmount[i] += price.toDouble() / cnt
                                if (i != index) {
                                    totalAmount[i] -= price.toDouble() / (cnt - 1)
                                }
                            }
                        }
                        currentItem.indicesOfSelectedNames.add(index)
                    } else {
                        holder.namesBox.children.forEachIndexed { i, chk ->
                            if ((chk as CheckBox).isChecked) {
                                totalAmount[i] += price.toDouble() / cnt - price.toDouble() / (cnt + 1)
                            }
                        }
                        totalAmount[index] -= price.toDouble() / (cnt + 1)
                        currentItem.indicesOfSelectedNames.remove(index)
                    }
                    debtBox.adapter = BillAdapter(createDebtList())
                }
            }
        }

        inner class ItemViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            val itemName: TextView = itemView.itemNameTV
            val itemPrice: TextView = itemView.itemPriceTV
            val namesBox: FlexboxLayout = itemView.namesBox

            init {
                names.forEachIndexed { index, name ->
                    val chk = CheckBox(itemView.context)
                    chk.text = name
                    namesBox.addView(chk)
                }
            }
        }
    }


}