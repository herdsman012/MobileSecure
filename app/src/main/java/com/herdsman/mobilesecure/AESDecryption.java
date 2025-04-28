package com.herdsman.mobilesecure;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class AESDecryption {

    static {
        System.loadLibrary("mobilesecure");
    }

    public static native byte[] getKey(Context context);

    public static native InputStream decryptFile(byte[] encryptedData, byte[] key);

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

    public static InputStream decrypt(Context context, InputStream in) throws Exception {
        byte[] encryptedData = new byte[in.available()];
        in.read(encryptedData);

        return new ByteArrayInputStream(encryptedData);
//        byte[] key = getKey(context);
//        return decryptFile(encryptedData, key);
    }

}
