# FastKeychain Roadmap 

**Vision:** The defacto hardware-backed key and credential management library for high-performance Java applications on desktop and cloud servers.

##  v0.1.0: Windows Foundation (Current)
- [x] Windows DPAPI direct integration (`CryptProtectData` / `CryptUnprotectData`).
- [x] Windows Credential Vault (`CredWriteW`, `CredReadW`, `CredDeleteW`).
- [x] Native zero-allocation memory wiping (`SecureZeroMemory`).
- [x] Embedded x64 native binary and automated extraction via `FastCore`.
- [x] Standalone benchmark and demo suites.

##  v0.2.0: Extended Security Primitives
- [ ] DPAPI-NG (Data Protection API: Next Generation) support for enterprise Active Directory / group protection.
- [ ] Support for secondary entropy keys and prompt flags (`CRYPTPROTECT_PROMPTSTRUCT`).
- [ ] Safe char buffer / direct memory wrappers for password fields without string allocation.

##  v0.5.0: Cross-Platform Keychain Expansion
- [ ] macOS Keychain Services integration via Apple Security Framework (`SecKeychainAddGenericPassword`).
- [ ] Linux Secret Service (FreeDesktop D-Bus / libsecret) support.

##  v1.0.0: Enterprise Production Hardening
- [ ] Smartcard and FIDO2 / WebAuthn token integration.
- [ ] Automated security audit and FIPS-140 compliance documentation.
