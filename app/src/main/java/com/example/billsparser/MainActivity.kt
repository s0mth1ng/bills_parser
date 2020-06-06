package com.example.billsparser

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private val pickJsonFileCode = 111
    private val namesCount = 3
    private val names = arrayOf("Максим", "Заффар", "Никита")
    private val prices = ArrayList<Int>()
    private var totalAmount = Array(namesCount) { 0.0F }
    private val itemsList = ArrayList<ItemView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadButton.setOnClickListener {
            totalAmount = Array(namesCount) { 0.0F }
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
        val list = ArrayList<BillView>()
        for (i in 0 until namesCount) {
            list.add(BillView(names[i], totalAmount[i]))
        }
        return list
    }

    private fun handleObject(item: JSONObject) {
        val price = item.get("price") as Int
        prices.add(price)
        val name = item.get("name").toString()
        val spaceInd = name.indexOf(' ')
        itemsList.add(ItemView(name.substring(spaceInd + 1), item.get("price") as Int))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == pickJsonFileCode && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                val jsonString = readTextFromUri(uri)
                val items = JSONObject(jsonString).getJSONArray("items")
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    handleObject(item)
                }
                itemsBox.adapter = ItemAdapter(itemsList)
                itemsBox.layoutManager = LinearLayoutManager(this)
                itemsBox.setHasFixedSize(true)
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

}