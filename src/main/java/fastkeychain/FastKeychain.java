package fastkeychain;

import fastcore.FastCore;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * FastKeychain — Hardware-backed Windows Credential Manager and DPAPI key management.
 * 
 * Provides:
 * 1. DPAPI (Data Protection API): Transparent encryption tied to current user and hardware TPM.
 * 2. Credential Manager: Persistent storage in Windows Credential Vault (CredReadW/CredWriteW).
 * 3. Memory Scrubbing: Automatic zeroing of sensitive buffers to prevent RAM dumps.
 */
public final class FastKeychain {

    static {
        FastCore.loadLibrary("fastkeychain");
    }

    private FastKeychain() {}

    /**
     * Encrypts arbitrary bytes using Windows DPAPI (CryptProtectData).
     * The ciphertext is bound to the current Windows user account and machine hardware.
     *
     * @param plaintext Sensitive data bytes
     * @return Hardware-encrypted ciphertext bytes, or null on failure
     */
    public static byte[] protectData(byte[] plaintext) {
        if (plaintext == null || plaintext.length == 0) {
            return new byte[0];
        }
        return nativeProtectData(plaintext);
    }

    /**
     * Decrypts ciphertext previously encrypted with protectData (CryptUnprotectData).
     *
     * @param ciphertext DPAPI-encrypted bytes
     * @return Decrypted plaintext bytes, or null on failure
     */
    public static byte[] unprotectData(byte[] ciphertext) {
        if (ciphertext == null || ciphertext.length == 0) {
            return new byte[0];
        }
        return nativeUnprotectData(ciphertext);
    }

    /**
     * Encrypts a plaintext string (e.g. license key or token) using Windows DPAPI.
     */
    public static byte[] protectString(String plaintext) {
        if (plaintext == null) return null;
        byte[] bytes = plaintext.getBytes(StandardCharsets.UTF_8);
        try {
            return protectData(bytes);
        } finally {
            wipe(bytes);
        }
    }

    /**
     * Decrypts a DPAPI ciphertext back to a UTF-8 String.
     */
    public static String unprotectString(byte[] ciphertext) {
        byte[] plain = unprotectData(ciphertext);
        if (plain == null) return null;
        try {
            return new String(plain, StandardCharsets.UTF_8);
        } finally {
            wipe(plain);
        }
    }

    /**
     * Writes a credential (e.g. application license or API token) into the Windows Credential Manager.
     *
     * @param targetName Unique vault identifier (e.g. "FastScreenCaptureStudio/License")
     * @param userName   User identifier (e.g. "LicenseHolder" or username)
     * @param secret     Secret payload bytes
     * @return true if written successfully
     */
    public static boolean writeSecret(String targetName, String userName, byte[] secret) {
        if (targetName == null || targetName.isBlank() || secret == null) {
            return false;
        }
        return nativeWriteCredential(targetName, userName, secret);
    }

    /**
     * Writes a string secret into the Windows Credential Manager.
     */
    public static boolean writeString(String targetName, String userName, String secret) {
        if (secret == null) return false;
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        try {
            return writeSecret(targetName, userName, bytes);
        } finally {
            wipe(bytes);
        }
    }

    /**
     * Reads a secret from the Windows Credential Manager.
     *
     * @param targetName Unique vault identifier
     * @return Secret bytes, or null if not found
     */
    public static byte[] readSecret(String targetName) {
        if (targetName == null || targetName.isBlank()) {
            return null;
        }
        return nativeReadCredential(targetName);
    }

    /**
     * Reads a secret from the Windows Credential Manager as a UTF-8 String.
     */
    public static String readString(String targetName) {
        byte[] bytes = readSecret(targetName);
        if (bytes == null) return null;
        try {
            return new String(bytes, StandardCharsets.UTF_8);
        } finally {
            wipe(bytes);
        }
    }

    /**
     * Deletes a credential from the Windows Credential Manager.
     */
    public static boolean deleteSecret(String targetName) {
        if (targetName == null || targetName.isBlank()) {
            return false;
        }
        return nativeDeleteCredential(targetName);
    }

    /**
     * Memory scrubbing helper: Securely zeroes out byte array memory.
     */
    public static void wipe(byte[] buffer) {
        if (buffer != null) {
            Arrays.fill(buffer, (byte) 0);
        }
    }

    // Native JNI functions
    private static native byte[] nativeProtectData(byte[] plaintext);
    private static native byte[] nativeUnprotectData(byte[] ciphertext);
    private static native boolean nativeWriteCredential(String target, String user, byte[] secret);
    private static native byte[] nativeReadCredential(String target);
    private static native boolean nativeDeleteCredential(String target);
}
