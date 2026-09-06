# Changelog

All notable changes to **FastKeychain** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.1.0] - 2026-09-06

### Added
- Native Windows DPAPI direct encryption and decryption (`protectData`, `unprotectData`, `protectString`, `unprotectString`).
- Windows Credential Manager vault integration (`writeSecret`, `readSecret`, `deleteSecret`).
- Native cryptographic memory scrubbing (`wipe`) using `SecureZeroMemory`.
- Dynamic native DLL loader integration with `FastCore`.
- Comprehensive benchmark and demo suites with verified sub-millisecond execution times.
- Full documentation suite (`README.md`, `docs/REFERENCE.md`, `docs/PHILOSOPHY.md`, `docs/ROADMAP.md`).
