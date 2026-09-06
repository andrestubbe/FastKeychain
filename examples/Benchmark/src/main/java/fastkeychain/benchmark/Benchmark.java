package fastkeychain.benchmark;

import fastkeychain.FastKeychain;

public class Benchmark {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println(" FastKeychain 0.1.0 — Latency & Throughput Benchmark");
        System.out.println("==================================================");

        byte[] payload = "LicenseKey-Hardware-Bound-Encrypted-FastJava-Payload-2026".getBytes();
        int warmup = 1000;
        int iterations = 10_000;

        // Warmup
        for (int i = 0; i < warmup; i++) {
            byte[] enc = FastKeychain.protectData(payload);
            FastKeychain.unprotectData(enc);
        }

        // DPAPI Protect Benchmark
        long startProtect = System.nanoTime();
        byte[] lastEnc = null;
        for (int i = 0; i < iterations; i++) {
            lastEnc = FastKeychain.protectData(payload);
        }
        long endProtect = System.nanoTime();
        double protectNanosPerOp = (double) (endProtect - startProtect) / iterations;

        // DPAPI Unprotect Benchmark
        long startUnprotect = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            FastKeychain.unprotectData(lastEnc);
        }
        long endUnprotect = System.nanoTime();
        double unprotectNanosPerOp = (double) (endUnprotect - startUnprotect) / iterations;

        // Windows Credential Manager Write/Read Benchmark
        String benchTarget = "FastKeychain/BenchTarget";
        long startVault = System.nanoTime();
        int vaultOps = 500;
        for (int i = 0; i < vaultOps; i++) {
            FastKeychain.writeSecret(benchTarget, "BenchUser", payload);
            FastKeychain.readSecret(benchTarget);
        }
        long endVault = System.nanoTime();
        FastKeychain.deleteSecret(benchTarget);
        double vaultMsPerRoundtrip = (double) (endVault - startVault) / (vaultOps * 1_000_000.0);

        System.out.printf("\n[DPAPI Hardware Protect  ] %.2f µs / op (%,d ops/sec)\n",
                protectNanosPerOp / 1000.0, (long) (1_000_000_000.0 / protectNanosPerOp));
        System.out.printf("[DPAPI Hardware Unprotect] %.2f µs / op (%,d ops/sec)\n",
                unprotectNanosPerOp / 1000.0, (long) (1_000_000_000.0 / unprotectNanosPerOp));
        System.out.printf("[Windows Vault Roundtrip ] %.3f ms / write+read cycle\n", vaultMsPerRoundtrip);
        System.out.println("==================================================");
    }
}
