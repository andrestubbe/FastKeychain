# The Philosophy of FastKeychain

> [!IMPORTANT]
> **"Sensible Daten gehören nicht in den Java-Heap. Hardware-Trust und sofortiges Memory-Scrubbing sind Pflicht."**

FastKeychain was designed to solve one of the most critical vulnerabilities in standard enterprise Java applications: **the persistence and exposure of plaintext credentials in JVM heap memory and configuration files**.

## Core Tenets

1. **Hardware-Anchored Security**
   Instead of inventing custom encryption schemes or storing keys in `.env` files, FastKeychain delegates security to the host platform's cryptographic backbone (Windows DPAPI and TPM 2.0).

2. **Immediate Memory Sanitization**
   Java's immutable `java.lang.String` lives in memory indefinitely until GC sweeps it, exposing secrets to memory dump exploits. FastKeychain encourages `byte[]` payloads with immediate zeroization via native `SecureZeroMemory`.

3. **Seamless Windows Vault Integration**
   Enterprise applications must play nice with corporate credential policies. Storing credentials directly in the Windows Credential Manager provides compliance and transparent OS-managed lifecycle.

4. **Zero-Dependency Simplicity**
   FastKeychain requires zero external DLL dependencies outside the standard Windows C runtime and Windows Crypto API (`Crypt32.lib`, `Advapi32.lib`).
