package com.herdsman.mobilesecure;

import android.content.Context;

public class AESDecryption {

    static {
        System.loadLibrary("mobilesecure");
    }

    public static native byte[] getKey(Context context);

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');  // pad with zero
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
