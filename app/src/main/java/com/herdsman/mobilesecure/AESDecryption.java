package com.herdsman.mobilesecure;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

public class AESDecryption {

    static {
        System.loadLibrary("mobilesecure");
    }

    public static native byte[] getKey(Context context);

    public static InputStream decryptFile(byte[] encryptedData, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        // Extract the IV (the first 16 bytes are the IV in the encrypted data)
        byte[] iv = new byte[16];
        System.arraycopy(encryptedData, 0, iv, 0, iv.length);

        // Create a Cipher with the IV and key for decryption
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new javax.crypto.spec.IvParameterSpec(iv));

        // Decrypt the data
        return new CipherInputStream(new ByteArrayInputStream(encryptedData, 16, encryptedData.length - 16), cipher);
    }

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

        byte[] key = getKey(context);
        return decryptFile(encryptedData, key);
    }

}
