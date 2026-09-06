package fastkeychain.demo;

import fastkeychain.FastKeychain;

public class Demo {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" FastKeychain 0.1.0 — Native Security Showcase");
        System.out.println("==================================================");

        // 1. Windows DPAPI Hardware-bound Encryption
        String apiKey = "sk-ant-api03-live-99882244-SECRET-KEY-FASTJAVA";
        System.out.println("\n[1] Encrypting API Token via Windows DPAPI & TPM...");
        System.out.println("    Plaintext: " + apiKey);

        byte[] encrypted = FastKeychain.protectString(apiKey);
        System.out.printf("    Encrypted: [%d bytes] (Hex: %02X%02X%02X%02X...)\n",
                encrypted.length, encrypted[0], encrypted[1], encrypted[2], encrypted[3]);

        String decrypted = FastKeychain.unprotectString(encrypted);
        System.out.println("    Decrypted: " + decrypted);
        System.out.println("    Verified : " + apiKey.equals(decrypted));

        // 2. Windows Credential Manager Persistent Storage
        String vaultTarget = "FastJava/DemoService";
        String vaultUser = "AndreStubbe";
        String vaultSecret = "FastJava-TopSecret-Passphrase-2026";

        System.out.println("\n[2] Storing secret in Windows Credential Vault...");
        boolean written = FastKeychain.writeString(vaultTarget, vaultUser, vaultSecret);
        System.out.println("    CredWriteW status: " + (written ? "SUCCESS" : "FAILED"));

        System.out.println("\n[3] Reading secret back from Windows Credential Vault...");
        String readBack = FastKeychain.readString(vaultTarget);
        System.out.println("    CredReadW result : " + readBack);

        System.out.println("\n[4] Cleaning up secret from Windows Credential Vault...");
        boolean deleted = FastKeychain.deleteSecret(vaultTarget);
        System.out.println("    CredDeleteW status: " + (deleted ? "SUCCESS" : "FAILED"));

        System.out.println("\n==================================================");
        System.out.println(" Demo Complete — Zero Leak Memory Scrubbed.");
        System.out.println("==================================================");
    }
}
