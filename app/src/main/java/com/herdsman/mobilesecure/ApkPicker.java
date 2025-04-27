package com.herdsman.mobilesecure;

import android.os.Environment;

import java.io.File;

public class ApkPicker {
    public static String getPath() {
        File file = new File(Environment.getExternalStorageDirectory(), "Pictures/mobilesecure.apk");
        if (file.exists()) {
            return file.getAbsolutePath();
        } else {
            return null;
        }
    }
}
