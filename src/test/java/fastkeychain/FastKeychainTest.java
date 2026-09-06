package fastkeychain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FastKeychainTest {

    @Test
    public void testDpapiProtectUnprotectString() {
        String secret = "FastScreenCapture-License-Key-2026-XYZ";
        byte[] encrypted = FastKeychain.protectString(secret);

        assertNotNull(encrypted);
        assertTrue(encrypted.length > secret.length());

        String decrypted = FastKeychain.unprotectString(encrypted);
        assertEquals(secret, decrypted);
    }

    @Test
    public void testCredentialManagerStorage() {
        String target = "FastScreenCaptureStudio/UnitTestSecret";
        String user = "TestUser";
        String secret = "Token-987654321-Secure";

        boolean written = FastKeychain.writeString(target, user, secret);
        assertTrue(written);

        String readBack = FastKeychain.readString(target);
        assertEquals(secret, readBack);

        boolean deleted = FastKeychain.deleteSecret(target);
        assertTrue(deleted);

        assertNull(FastKeychain.readString(target));
    }
}
