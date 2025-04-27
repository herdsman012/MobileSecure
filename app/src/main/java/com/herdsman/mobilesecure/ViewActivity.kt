package com.herdsman.mobilesecure

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.herdsman.mobilesecure.databinding.ActivityViewBinding


class ViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var index = 0

        if (intent.hasExtra("index")) {
            index = intent.getIntExtra("index", 0)
        }

        val item = MainActivity.itemList[index]

        supportActionBar?.title = item.title

        val html = Html.fromHtml(DbHelper.readString(this, "tmp.html"), Html.FROM_HTML_MODE_LEGACY, object : Html.ImageGetter {
            override fun getDrawable(source: String?): Drawable? {
                source?.let {
                    Log.d("herdsman_log", it)
                    val inputStream = DbHelper.readInputStream(this@ViewActivity, source)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val drawable: Drawable = bitmap.toDrawable(resources)
                    drawable.setBounds(0, 0, bitmap.width, bitmap.height)
                    return drawable
                }
                return null
            }

        }, null)

        binding.textView.text = html
    }
}