package com.herdsman.mobilesecure;

import java.io.File;

public class ApkPicker {
    public static String path = null;

    public static String getPath() {
        File file = new File(path);
        if (file.exists()) {
            return file.getAbsolutePath();
        } else {
            return null;
        }
    }
}
