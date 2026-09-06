# FastKeychain Reference

## 1. Security Architecture & Threat Model

FastKeychain provides direct native bindings to the Microsoft Windows security ecosystem:

*   **Windows DPAPI (`CryptProtectData` / `CryptUnprotectData`)**:
    - Data is encrypted using symmetric keys derived from the user's logon credentials.
    - Transparent hardware integration: Where available, master keys are backed and sealed by the machine's **TPM 2.0 (Trusted Platform Module)**.
    - Protects secrets at rest against physical storage theft, memory dumps, and offline disk analysis.
*   **Windows Credential Manager (`CredWriteW` / `CredReadW` / `CredDeleteW`)**:
    - Stores generic user credentials securely inside the encrypted Windows Credential Vault (`%LocalAppData%\Microsoft\Vault`).
    - Secrets are maintained across application restarts and OS reboots.
*   **Zero-Copy Memory Scrubbing (`SecureZeroMemory`)**:
    - Standard Java strings cannot be securely erased from heap memory until garbage collected.
    - FastKeychain provides zero-allocation memory wiping via `FastKeychain.wipe(byte[])` to zero-out plaintext immediately after cryptographic consumption.

---

## 2. API Contract

### DPAPI Methods
```java
// Encrypts raw bytes using the current Windows user context
byte[] FastKeychain.protectData(byte[] plaintext, byte[] optionalEntropy);

// Decrypts bytes encrypted by FastKeychain.protectData
byte[] FastKeychain.unprotectData(byte[] ciphertext, byte[] optionalEntropy);

// Convenience methods for UTF-8 Strings
byte[] FastKeychain.protectString(String secret);
String FastKeychain.unprotectString(byte[] ciphertext);
```

### Windows Credential Manager Methods
```java
// Persist secret in Windows Credential Vault
boolean FastKeychain.writeSecret(String targetName, byte[] secret);

// Retrieve secret from Windows Credential Vault
byte[] FastKeychain.readSecret(String targetName);

// Delete secret from Windows Credential Vault
boolean FastKeychain.deleteSecret(String targetName);
```

### Security Utilities
```java
// Cryptographically wipes byte array to 0x00 using SecureZeroMemory
void FastKeychain.wipe(byte[] secret);
```

---

## 3. Platform Support
| Platform | Hardware Acceleration | Status |
|----------|----------------------|--------|
| Windows 10/11 x64 | TPM 2.0 / DPAPI |  Fully Supported |
| Windows Server 2016+ x64 | TPM / DPAPI |  Fully Supported |

---
**Part of the FastJava Ecosystem**  *Hardware-accelerated Java runtime libraries.*
