package com.herdsman.mobilesecure

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.herdsman.mobilesecure.databinding.ActivityMainBinding
import com.herdsman.mobilesecure.databinding.LayoutItemBinding
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("mobilesecure")
        }

        var itemList = listOf<Item>()
    }

    class Item {
        lateinit var title: String
        lateinit var content: String
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            itemList = Gson().fromJson(DbHelper.readString(this@MainActivity, "list.json"), arrayOf<Item>()::class.java).toList()
            binding.recyclerView.adapter = ItemAdapter()
        }

    }

    class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

        class ViewHolder(private var binding: LayoutItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(position: Int) {
                val item = itemList!![position]
                binding.titleView.text = item.title
                binding.contentView.text = item.content

                binding.root.setOnClickListener {
                    with(binding.root.context) {
                        val intent = Intent(this, ViewActivity::class.java)
                        intent.putExtra("index", position)
                        this.startActivity(intent)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = itemList!!.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(position)
        }
    }

    external fun stringFromJNI(): String


}