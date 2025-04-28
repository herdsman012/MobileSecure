package com.herdsman.mobilesecure

import android.content.Context
import android.util.Log
import org.apache.commons.io.IOUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object DbHelper {
    private fun readSpecificFileFromZip(context: Context, inStream: InputStream?, targetFileName: String): InputStream? {
        val decryptedZipStream = AESDecryption.decrypt(context, inStream)
        val zipInputStream = ZipInputStream(decryptedZipStream)
        var zipEntry: ZipEntry

        while ((zipInputStream.nextEntry.also { zipEntry = it }) != null) {
            if (zipEntry.name == targetFileName) {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var bytesRead: Int

                while ((zipInputStream.read(buffer).also { bytesRead = it }) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead)
                }

                return ByteArrayInputStream(byteArrayOutputStream.toByteArray())
            }
        }
        return null
    }

    fun readInputStream(context: Context, path: String): InputStream? = readSpecificFileFromZip(
        context,
        context.assets.open("data.enc"), path
    )

    fun readString(context: Context, path: String): String = IOUtils.toString(readInputStream(context, path), StandardCharsets.UTF_8)
}