package com.herdsman.mobilesecure

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.herdsman.mobilesecure.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getKeyBtn.setOnClickListener {
            if (ApkPicker.getPath() == null) {
                binding.editText.setText("can't find <mobilesecure.apk> file")
            } else {
                Log.d("herdsman_log", packageCodePath)
                Log.d("herdsman_log", AESDecryption.bytesToHex(AESDecryption.getKey(this)))
                binding.editText.setText(AESDecryption.bytesToHex(AESDecryption.getKey(this)))
            }
        }

    }
}