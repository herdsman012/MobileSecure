#include <jni.h>
#include <string>
#include <vector>
#include <zip.h>      // Using libzip
#include "sha256.h"
#include <android/log.h>

#define LOG_TAG "SecureApp"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Helper: Get APK path
std::string getApkPath(JNIEnv *env, jobject context) {
    jclass contextClass = env->GetObjectClass(context);
    jmethodID getPackageCodePath = env->GetMethodID(contextClass, "getPackageCodePath", "()Ljava/lang/String;");
    jstring apkPathJString = (jstring) env->CallObjectMethod(context, getPackageCodePath);

    const char *apkPathCStr = env->GetStringUTFChars(apkPathJString, nullptr);
    std::string apkPath(apkPathCStr);
    env->ReleaseStringUTFChars(apkPathJString, apkPathCStr);

    return apkPath;
}

// Helper: SHA256 hash a memory buffer
std::string sha256Buffer(const unsigned char *data, size_t length) {
    unsigned char hash[32];
    SHA256_CTX ctx;
    sha256_init(&ctx);
    sha256_update(&ctx, (const unsigned char *) data, length);
    sha256_final(&ctx, hash);

    char hexStr[65];
    for (int i = 0; i < 32; ++i) {
        sprintf(hexStr + i * 2, "%02x", hash[i]);
    }
    hexStr[64] = 0;

    return std::string(hexStr);
}

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

// Main JNI function: Generate dynamic key
extern "C" JNIEXPORT jbyteArray JNICALL Java_com_herdsman_mobilesecure_AESDecryption_getKey(JNIEnv *env, jclass, jobject context) {
    std::string apkPath = getApkPath(env, context);

    int error = 0;
    zip_t *apk = zip_open(apkPath.c_str(), ZIP_RDONLY, &error);
    if (!apk) {
        return nullptr;
    }

    std::vector<std::string> relevantHashes;

    zip_int64_t numEntries = zip_get_num_entries(apk, 0);
    for (zip_int64_t i = 0; i < numEntries; ++i) {
        const char *filename = zip_get_name(apk, i, 0);
        if (!filename) continue;

        std::string name(filename);

        // Check if file is interesting
        if (name == "AndroidManifest.xml" ||
            name.find("classes") == 0 && name.find(".dex") != std::string::npos ||
            (name.find(".so") != std::string::npos && name.find("lib/") == 0)) {

            zip_file_t *file = zip_fopen_index(apk, i, 0);
            if (!file) continue;

            zip_stat_t st;
            zip_stat_index(apk, i, 0, &st);

            std::vector<unsigned char> buffer(st.size);
            zip_fread(file, buffer.data(), st.size);
            zip_fclose(file);

            std::string fileHash = sha256Buffer(buffer.data(), st.size);
            relevantHashes.push_back(fileHash);
        }
    }

    zip_close(apk);

    // Concatenate all hashes
    std::string combined;
    for (const auto &hash: relevantHashes) {
        combined += hash;
    }

    // Final key
    std::string finalKey = sha256Buffer(reinterpret_cast<const unsigned char *>(combined.c_str()), combined.length());

    size_t byteLength = finalKey.length() / 2;

    uint8_t *bytes = (uint8_t *) malloc(byteLength);
    hexStringToBytes(finalKey.c_str(), bytes, byteLength);

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