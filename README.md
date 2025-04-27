# README: OWASP Mobile Top 10 - M10: Insufficient Cryptography Project

## Project Overview

This project demonstrates mobile application vulnerabilities aligned with **OWASP Mobile Top 10 - M10: Insufficient Cryptography**, with special focus on:

- **Scenario #4:** Key Management Vulnerabilities
- **Scenario #5:** Crypto Implementation Flaws

The project showcases two versions:

- **Vulnerable Version:** Static key encryption inside JNI code with weak obfuscation, easily exploitable.
- **Secure Version:** Dynamic key generation at runtime based on SHA-256 checksums of APK components, with no static keys stored inside the APK.

---

## Features

- AES Encryption/Decryption of study materials.
- Vulnerable Version with Static Key (Weak XOR Obfuscation).
- Secure Version with Dynamic Key Generation.
- Native code handling for sensitive operations.
- Tamper detection through file checksum validation.
- Attack simulations included.

---

## Project Structure

```bash
app/
├── src/main/java/com/herdsman.mobilesecure/  # Java/Kotlin Decryption Classes
├── src/main/cpp/                             # Native C++ Key Derivation Logic
├── assets/data.enc                           # Encrypted study materials
├── build.gradle                              # Project configuration
```

---

## How It Works

### Vulnerable Version

- AES key statically stored inside JNI native library.
- Decryption logic written in Java.
- Key obfuscated with simple XOR.
- Easily reversible using APK decompilation and dummy project techniques.

### Secure Version

- APK opened at runtime using `libzip`.
- SHA-256 checksums collected for `AndroidManifest.xml`, `classes.dex`, and `.so` files.
- Combined checksum used to dynamically derive AES key.
- Key never stored statically.
- Any APK modification invalidates decryption key.

---

## How to Run

1. Clone this repository.
2. Open the project in Android Studio.
3. Build and install the app on a real device or emulator.
4. Observe secure decryption process.

> **Note:** Ensure Android NDK and CMake are configured correctly.

---

## Attack Simulation

### Vulnerable Version:

- APK decompiled using JADX.
- JNI `getKey()` extracted.
- Static AES key recovered.
- Encrypted files decrypted offline.

### Secure Version:

- No static key.
- Tampering causes decryption failure.
- Key derivation tied to original APK integrity.

---

## Skills Demonstrated

- Mobile app cryptography
- JNI integration and native security handling
- Reverse engineering analysis
- Secure application architecture
- Real-world OWASP M10 vulnerability simulation and mitigation

---

## Future Enhancements

- Device-specific binding (e.g., using Android ID)
- Implement AES-GCM for authenticated encryption
- Enhanced control flow obfuscation in native code

---

## References

- [OWASP Mobile Security Testing Guide](https://owasp.org/www-project-mobile-security-testing-guide/)
- [OWASP MASVS - Mobile Application Security Verification Standard](https://owasp.org/www-project-mobile-security/)
- [NIST SP 800-57 Part 1 Revision 5 - Key Management Guidelines](https://csrc.nist.gov/publications/detail/sp/800-57-part-1/rev-5/final)

---

## Contact

**Developer:** [Herdsman]