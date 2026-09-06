# FastKeychain 0.1.0 — Hardware-Backed Windows Credential Manager & DPAPI Key Management for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastKeychain/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010%20%2F%2011%20%28x64%29-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.0-green.svg)](https://jitpack.io/#andrestubbe/FastKeychain)

---

**⚡ Hardware-bound encryption via Windows DPAPI & persistent credentials in the Windows Credential Vault with zero-heap memory scrubbing.**

`FastKeychain` eliminates the widespread practice of storing sensitive API tokens, application licenses, and passphrases in insecure plaintext files or raw configuration keys. It binds encrypted secrets directly to the current Windows user profile and the machine's hardware **TPM 2.0 security chip**, while scrubbing memory buffers immediately after use to prevent RAM dump inspection.

---

## Quick Start

### 1. Run Interactive Showcase Demo
```cmd
run-demo.bat
```

### 2. Run Latency & Throughput Benchmark
```cmd
run-benchmark.bat
```

### 3. Programmatic Java API
```java
import fastkeychain.FastKeychain;

public class Example {
    public static void main(String[] args) {
        // Transparent Windows DPAPI (CryptProtectData / CryptUnprotectData)
        String sensitiveToken = "sk-ant-api03-live-99882244-SECRET";
        byte[] encrypted = FastKeychain.protectString(sensitiveToken);
        String decrypted = FastKeychain.unprotectString(encrypted);

        // Persistent Windows Credential Manager (CredWriteW / CredReadW)
        FastKeychain.writeSecret("MyApp/License", "FSC-PRO-KEY-2026".getBytes());
        byte[] licenseBytes = FastKeychain.readSecret("MyApp/License");
        FastKeychain.deleteSecret("MyApp/License");

        // Zero-allocation memory wiping
        FastKeychain.wipe(licenseBytes);
    }
}
```

---

## Table of Contents

- [Why FastKeychain?](#why-fastkeychain)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Architecture & Security Pipeline](#architecture--security-pipeline)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Technical Examples & Hero Demos](#technical-examples--hero-demos)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---

## Why FastKeychain?

Standard Java applications suffer from fundamental security vulnerabilities when handling credentials:
1. **Plaintext Leaks on Disk**: Developers persist API keys or licenses in `.json`, `.properties`, or `.env` files readable by any process or malware.
2. **RAM Dump Vulnerability**: Standard `java.lang.String` instances are immutable and remain cached in the JVM garbage collector heap indefinitely.
3. **No Hardware Binding**: Encrypted files can simply be copied to other machines without restriction.

**FastKeychain solves this:**
- **TPM 2.0 & OS User Binding**: Uses `CryptProtectData` (DPAPI) — data can only be decrypted on the exact same hardware by the same logged-in Windows user.
- **Windows Credential Manager Integration**: Native persistence via `CredWriteW` / `CredReadW` residing in the secure Windows Credential Vault.
- **Zero-String Memory Scrubbing**: Automatically scrubs native buffers with `SecureZeroMemory` and primitive Java arrays with `Arrays.fill((byte)0)`.

---

## Key Features

- 🛡️ **Windows DPAPI Direct Access** — Hardware-tied `CryptProtectData` and `CryptUnprotectData` in sub-millisecond execution.
- 🗄️ **Windows Vault Integration** — Read, write, and delete credentials directly in the Windows Credential Manager.
- 🧹 **Zero-Leak Memory Scrubbing** — Immediate memory zeroization (`FastKeychain.wipe`) preventing RAM forensics and heap dumps.
- ⚡ **Zero-Copy Native Bridge** — High-speed JNI bridge with zero external DLL dependencies (links directly to `Crypt32.lib` & `Advapi32.lib`).
- 🔗 **FastJava Ecosystem Synergy** — Seamlessly integrates with `FastCore`, `FastCrypto`, and `FastScreenCaptureApp`.

---

## Real-World Use Cases

- 🔑 **AI Agent & LLM API Keys ([FastAgent](https://github.com/andrestubbe/FastAgent))**: Store Anthropic, OpenAI, or Google API keys safely in the Windows Vault without saving them in `.env` or project source code.
- 💼 **Commercial Software License Validation**: Protect and verify offline license tokens (`FSC-PRO-...`) with hardware TPM anchoring so they cannot be copied to another machine.
- 🔐 **Master Key Storage for FastCrypto**: Securely store 256-bit AES master keys and auto-unlock them at runtime without prompting the user for passwords on every start.
- 🌐 **Enterprise Cloud / DB Credentials**: Retrieve database connection passwords and service credentials on-demand and wipe them from RAM immediately after socket connection.

---

## Architecture & Security Pipeline

```text
┌─────────────────────────────────────────────────────────────┐
│                    Java Application Layer                   │
│                     (FastKeychain.java)                     │
└──────────────────────────────┬──────────────────────────────┘
                               │ JNI Bridge (< 1 µs)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                fastkeychain.dll (MSVC x64)                  │
│       ├── CryptProtectDataW / CryptUnprotectDataW           │
│       ├── CredWriteW / CredReadW / CredDeleteW              │
│       └── SecureZeroMemory() Buffers                        │
└──────────────────────────────┬──────────────────────────────┘
                               │ Hardware-Backed Storage
                               ▼
┌─────────────────────────────────────────────────────────────┐
│         Windows Security Subsystem & TPM 2.0 Chip           │
│           (DPAPI Master Keys & Credential Vault)            │
└─────────────────────────────────────────────────────────────┘
```

---

## Performance Benchmarks

Measured on Intel Core i7 / Windows 11 with hardware TPM 2.0 (`run-benchmark.bat`):

| Operation | Average Latency | Throughput | Security Level |
|:---|:---:|:---:|:---|
| **DPAPI Hardware Protect** | **174.0 µs** | **~5,750 ops/sec** | Hardware TPM 2.0 + User DPAPI |
| **DPAPI Hardware Unprotect** | **144.5 µs** | **~6,920 ops/sec** | Hardware TPM 2.0 + User DPAPI |
| **Windows Vault Write + Read** | **5.75 ms** | **~175 cycles/sec** | Persistent Windows Credential Vault |

---

## API Quick Reference

| Method | Return Type | Description |
|:---|:---|:---|
| `protectData(byte[] data, byte[] entropy)` | `byte[]` | Encrypts raw byte array via Windows DPAPI |
| `unprotectData(byte[] data, byte[] entropy)` | `byte[]` | Decrypts DPAPI ciphertext byte array |
| `protectString(String secret)` | `byte[]` | Convenience method: Encrypts UTF-8 string with DPAPI |
| `unprotectString(byte[] ciphertext)` | `String` | Convenience method: Decrypts DPAPI ciphertext to String |
| `writeSecret(String target, byte[] secret)` | `boolean` | Persists generic credential into Windows Vault |
| `readSecret(String target)` | `byte[]` | Reads generic credential from Windows Vault |
| `deleteSecret(String target)` | `boolean` | Removes credential from Windows Vault |
| `wipe(byte[] secret)` | `void` | Overwrites memory buffer with 0x00 via `SecureZeroMemory` |

---

## Installation

FastKeychain is distributed via JitPack. It requires **FastCore** as the unified native library loader.

### Option 1: Maven (`pom.xml`)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastKeychain Core -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastKeychain</artifactId>
        <version>0.1.0</version>
    </dependency>

    <!-- FastCore Native Loader -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (`build.gradle`)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastKeychain:0.1.0'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest pre-compiled JARs:
1. 📦 [**FastKeychain-0.1.0.jar**](https://github.com/andrestubbe/FastKeychain/releases/tag/0.1.0)
2. ⚙️ [**FastCore-0.1.0.jar**](https://github.com/andrestubbe/FastCore/releases/tag/0.1.0)

---

## Technical Examples & Hero Demos

| Example / Demo | Description | Path | Run Command |
|---|---|---|---|
| **Interactive Showcase Demo** | Complete round-trip lifecycle demo showcasing DPAPI token sealing, Credential Vault persistence, and RAM scrubbing. | [`examples/Demo/Demo.java`](examples/Demo/src/main/java/fastkeychain/demo/Demo.java) | `run-demo.bat` |
| **Microsecond Benchmarks** | High-precision performance suite measuring hardware encryption latency and ops/sec. | [`examples/Benchmark/Benchmark.java`](examples/Benchmark/src/main/java/fastkeychain/benchmark/Benchmark.java) | `run-benchmark.bat` |

---

## Documentation

* **[`docs/REFERENCE.md`](docs/REFERENCE.md)**: Full API descriptions and security contracts.
* **[`docs/PHILOSOPHY.md`](docs/PHILOSOPHY.md)**: The engineering rationale for hardware-anchored zero-heap security.
* **[`docs/ROADMAP.md`](docs/ROADMAP.md)**: Future milestones (DPAPI-NG, macOS Keychain, Linux Secret Service).
* **[`docs/CHANGELOG.md`](docs/CHANGELOG.md)**: Detailed version history.

---

## Platform Support

| Platform | Hardware Security | Status |
|---|---|:---:|
| **Windows 10 / 11 (x64)** | TPM 2.0 / DPAPI / Credential Vault | ✅ Fully Supported |
| **Windows Server 2016+ (x64)** | TPM / DPAPI / Credential Vault | ✅ Fully Supported |
| **macOS / Linux** | Under Development (v0.5.0) | 🚧 Planned |

---

## Related Projects

- [FastCrypto](https://github.com/andrestubbe/FastCrypto) — Hardware-accelerated AES-GCM & SIMD cryptography
- [FastCore](https://github.com/andrestubbe/FastCore) — Native library loader with local fallback
- [FastScreen](https://github.com/andrestubbe/FastScreen) — DirectX DXGI screen capture engine
- [FastTheme](https://github.com/andrestubbe/FastTheme) — Windows native window styling & DWM theme engine

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster. ⚡*
