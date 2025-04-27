package com.herdsman.mobilesecure

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import com.herdsman.mobilesecure.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getKeyBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("application/vnd.android.package-archive") // MIME type for APK
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            startActivityForResult(Intent.createChooser(intent, "Select APK file"), 100)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null) {
                val apkUri = data.data
                if (apkUri != null) {
                    ApkPicker.path = apkUri.toFile().absolutePath
                    Log.d("herdsman_log", packageCodePath)
                    Log.d("herdsman_log", AESDecryption.bytesToHex(AESDecryption.getKey(this)))
                    binding.editText.setText(AESDecryption.bytesToHex(AESDecryption.getKey(this)))
                }
            }
        }
    }

}