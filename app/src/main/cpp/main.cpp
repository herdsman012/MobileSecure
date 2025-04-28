#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "SecureApp-herdsman"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Simple helper: Convert hex character to its integer value
uint8_t hexCharToByte(char c) {
    if (c >= '0' && c <= '9') return c - '0';
    if (c >= 'a' && c <= 'f') return 10 + (c - 'a');
    if (c >= 'A' && c <= 'F') return 10 + (c - 'A');
    return 0;
}

// Converts hex string to bytes
void hexStringToBytes(const char *hexStr, uint8_t *outBytes, size_t outLen) {
    for (size_t i = 0; i < outLen; ++i) {
        outBytes[i] = (hexCharToByte(hexStr[i * 2]) << 4) | hexCharToByte(hexStr[i * 2 + 1]);
    }
}

void simpleEncrypt(uint8_t *data, size_t length, uint8_t key) {
    for (size_t i = 0; i < length; ++i) {
        data[i] ^= key;
    }
}

extern "C" JNIEXPORT jbyteArray JNICALL Java_com_herdsman_mobilesecure_AESDecryption_getKey(JNIEnv *env, jclass) {
    const char *hexString = "d3cfb0650cd99568909579c59c9d14410e1bfccd77ff59a95de113bfa54b57d9";
    size_t byteLength = strlen(hexString) / 2;

    uint8_t *bytes = (uint8_t *) malloc(byteLength);
    hexStringToBytes(hexString, bytes, byteLength);

    // Encrypt with simple XOR key
    simpleEncrypt(bytes, byteLength, 0xAA);

    // Create a Java byte array to return
    jbyteArray result = env->NewByteArray(byteLength);
    env->SetByteArrayRegion(result, 0, byteLength, (const jbyte *) bytes);

    free(bytes);

    return result;
}

extern "C" JNIEXPORT jobject JNICALL Java_com_herdsman_mobilesecure_AESDecryption_decryptFile(JNIEnv *env, jclass clazz, jbyteArray encryptedData, jbyteArray key) {
    try {
        jclass cipherClass = env->FindClass("javax/crypto/Cipher");
        jmethodID getInstanceMethod = env->GetStaticMethodID(cipherClass, "getInstance", "(Ljava/lang/String;)Ljavax/crypto/Cipher;");
        jstring transformation = env->NewStringUTF("AES/CBC/PKCS5Padding");
        jobject cipherObject = env->CallStaticObjectMethod(cipherClass, getInstanceMethod, transformation);

        jclass secretKeySpecClass = env->FindClass("javax/crypto/spec/SecretKeySpec");
        jmethodID secretKeySpecCtor = env->GetMethodID(secretKeySpecClass, "<init>", "([BLjava/lang/String;)V");
        jstring aesString = env->NewStringUTF("AES");
        jobject secretKeySpec = env->NewObject(secretKeySpecClass, secretKeySpecCtor, key, aesString);

        jbyte ivBytes[16];
        env->GetByteArrayRegion(encryptedData, 0, 16, ivBytes);
        jbyteArray ivArray = env->NewByteArray(16);
        env->SetByteArrayRegion(ivArray, 0, 16, ivBytes);

        jclass ivSpecClass = env->FindClass("javax/crypto/spec/IvParameterSpec");
        jmethodID ivSpecCtor = env->GetMethodID(ivSpecClass, "<init>", "([B)V");
        jobject ivSpec = env->NewObject(ivSpecClass, ivSpecCtor, ivArray);

        jmethodID initMethod = env->GetMethodID(cipherClass, "init", "(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V");
        jint decryptMode = 2;
        env->CallVoidMethod(cipherObject, initMethod, decryptMode, secretKeySpec, ivSpec);

        jsize fullLen = env->GetArrayLength(encryptedData);

        jclass byteArrayInputStreamClass = env->FindClass("java/io/ByteArrayInputStream");
        jmethodID byteArrayInputStreamCtor = env->GetMethodID(byteArrayInputStreamClass, "<init>", "([BII)V");
        jobject byteArrayInputStream = env->NewObject(byteArrayInputStreamClass, byteArrayInputStreamCtor, encryptedData, 16, fullLen - 16);

        jclass cipherInputStreamClass = env->FindClass("javax/crypto/CipherInputStream");
        jmethodID cipherInputStreamCtor = env->GetMethodID(cipherInputStreamClass, "<init>", "(Ljava/io/InputStream;Ljavax/crypto/Cipher;)V");
        jobject cipherInputStream = env->NewObject(cipherInputStreamClass, cipherInputStreamCtor, byteArrayInputStream, cipherObject);

        return cipherInputStream;

    } catch (...) {
        // You can optionally throw Java exception here
        return nullptr;
    }
}