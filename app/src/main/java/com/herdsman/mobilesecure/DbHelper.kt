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

    private fun readSpecificFileFromZip(decryptedZipStream: InputStream?, targetFileName: String): InputStream? {
        val zipInputStream = ZipInputStream(decryptedZipStream)
        var zipEntry: ZipEntry

        while ((zipInputStream.nextEntry.also { zipEntry = it }) != null) {
            // Check if the current entry is the one you're looking for
            Log.d("herdsman_log", zipEntry.name)
            if (zipEntry.name == targetFileName) {
                // Return an InputStream for the specific file
                val byteArrayOutputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var bytesRead: Int

                // Write the contents of the file to the ByteArrayOutputStream
                while ((zipInputStream.read(buffer).also { bytesRead = it }) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead)
                }

                // Return the ByteArrayInputStream for the specific file content
                return ByteArrayInputStream(byteArrayOutputStream.toByteArray())
            }
        }

        // Return null if the file isn't found
        return null
    }

    fun readInputStream(context: Context, path: String): InputStream? = readSpecificFileFromZip(
        context.assets.open("data.zip"), path
    )

    fun readString(context: Context, path: String): String = IOUtils.toString(readInputStream(context, path), StandardCharsets.UTF_8)
}