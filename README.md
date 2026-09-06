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
        // Transparent Windows DPAPI (CryptProtectData)
        String sensitiveToken = "sk-ant-api03-live-99882244-SECRET";
        byte[] encrypted = FastKeychain.protectString(sensitiveToken);
        String decrypted = FastKeychain.unprotectString(encrypted);

        // Persistent Windows Credential Manager (CredWriteW / CredReadW)
        FastKeychain.writeString("MyApp/License", "RegisteredUser", "FSC-PRO-KEY-2026");
        String license = FastKeychain.readString("MyApp/License");
        FastKeychain.deleteSecret("MyApp/License");
    }
}
```

---

## Table of Contents

- [Why FastKeychain?](#why-fastkeychain)
- [Key Features](#key-features)
- [Architecture & Security Pipeline](#architecture--security-pipeline)
- [Performance Benchmarks](#performance-benchmarks)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

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
- 🧹 **Zero-Leak Memory Scrubbing** — Immediate memory zeroization preventing RAM forensics and heap dumps.
- ⚡ **Zero-Copy Native Bridge** — High-speed JNI bridge with zero external DLL dependencies (links directly to `Crypt32.lib` & `Advapi32.lib`).
- 🔗 **FastJava Ecosystem Synergy** — Seamlessly integrates with `FastCore`, `FastCrypto`, and `FastScreenCaptureApp`.

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

| Operation | Latency | Throughput | Security Level |
|---|:---:|:---:|---|
| **DPAPI Hardware Protect** | **174.0 µs** | **~5,750 ops/sec** | Hardware TPM 2.0 + User DPAPI |
| **DPAPI Hardware Unprotect** | **144.5 µs** | **~6,920 ops/sec** | Hardware TPM 2.0 + User DPAPI |
| **Windows Vault Write + Read** | **5.75 ms** | **~175 cycles/sec** | Persistent Windows Credential Vault |

---

## Installation

### Maven (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastKeychain</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

---

## Documentation

- [`docs/REFERENCE.md`](docs/REFERENCE.md) — Complete API documentation.
- [`docs/PHILOSOPHY.md`](docs/PHILOSOPHY.md) — Design principles and zero-leak security architecture.
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — Future milestone planning.
- [`docs/CHANGELOG.md`](docs/CHANGELOG.md) — Version history.

---

## Platform Support

- **Windows 10 / 11 (x86_64)**: Fully supported with hardware TPM 2.0 & DPAPI.
- **Native Library**: Pre-built `fastkeychain.dll` included in JAR resources.

---

## License

This project is licensed under the [MIT License](LICENSE).
