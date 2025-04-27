#include <jni.h>
#include <string>

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