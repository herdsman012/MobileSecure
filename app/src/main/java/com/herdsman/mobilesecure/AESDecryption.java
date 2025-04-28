package com.herdsman.mobilesecure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESDecryption {

    static {
        System.loadLibrary("mobilesecure");
    }

    public static native byte[] getKey();

    public static native InputStream decryptFile(byte[] encryptedData, byte[] key);


    public static InputStream decryptFile1(byte[] encryptedData, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        // Extract the IV (the first 16 bytes are the IV in the encrypted data)
        byte[] iv = new byte[16];
        System.arraycopy(encryptedData, 0, iv, 0, iv.length);

        // Create a Cipher with the IV and key for decryption
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

        // Decrypt the data
        return new CipherInputStream(new ByteArrayInputStream(encryptedData, 16, encryptedData.length - 16), cipher);
    }

    public static InputStream decrypt(InputStream in) throws Exception {
        byte[] encryptedData = new byte[in.available()];
        in.read(encryptedData);

        byte[] key = getKey();

        return decryptFile(encryptedData, key);
    }
}
