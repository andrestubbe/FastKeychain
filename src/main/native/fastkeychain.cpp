#include <jni.h>
#include <windows.h>
#include <wincrypt.h>
#include <wincred.h>

#pragma comment(lib, "Crypt32.lib")
#pragma comment(lib, "Advapi32.lib")

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     fastkeychain_FastKeychain
 * Method:    nativeProtectData
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_fastkeychain_FastKeychain_nativeProtectData
  (JNIEnv *env, jclass cls, jbyteArray plainArray) {
    if (!plainArray) return NULL;

    jsize len = env->GetArrayLength(plainArray);
    if (len == 0) return env->NewByteArray(0);

    jbyte *bytes = env->GetByteArrayElements(plainArray, NULL);
    if (!bytes) return NULL;

    DATA_BLOB inBlob;
    inBlob.pbData = (BYTE*)bytes;
    inBlob.cbData = (DWORD)len;

    DATA_BLOB outBlob;
    ZeroMemory(&outBlob, sizeof(outBlob));

    BOOL success = CryptProtectData(
        &inBlob,
        L"FastKeychain Data",
        NULL,
        NULL,
        NULL,
        CRYPTPROTECT_UI_FORBIDDEN,
        &outBlob
    );

    env->ReleaseByteArrayElements(plainArray, bytes, JNI_ABORT);

    if (!success) {
        return NULL;
    }

    jbyteArray result = env->NewByteArray(outBlob.cbData);
    if (result) {
        env->SetByteArrayRegion(result, 0, outBlob.cbData, (jbyte*)outBlob.pbData);
    }

    LocalFree(outBlob.pbData);
    return result;
}

/*
 * Class:     fastkeychain_FastKeychain
 * Method:    nativeUnprotectData
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_fastkeychain_FastKeychain_nativeUnprotectData
  (JNIEnv *env, jclass cls, jbyteArray cipherArray) {
    if (!cipherArray) return NULL;

    jsize len = env->GetArrayLength(cipherArray);
    if (len == 0) return env->NewByteArray(0);

    jbyte *bytes = env->GetByteArrayElements(cipherArray, NULL);
    if (!bytes) return NULL;

    DATA_BLOB inBlob;
    inBlob.pbData = (BYTE*)bytes;
    inBlob.cbData = (DWORD)len;

    DATA_BLOB outBlob;
    ZeroMemory(&outBlob, sizeof(outBlob));

    BOOL success = CryptUnprotectData(
        &inBlob,
        NULL,
        NULL,
        NULL,
        NULL,
        CRYPTPROTECT_UI_FORBIDDEN,
        &outBlob
    );

    env->ReleaseByteArrayElements(cipherArray, bytes, JNI_ABORT);

    if (!success) {
        return NULL;
    }

    jbyteArray result = env->NewByteArray(outBlob.cbData);
    if (result) {
        env->SetByteArrayRegion(result, 0, outBlob.cbData, (jbyte*)outBlob.pbData);
    }

    // Secure wipe memory before freeing
    SecureZeroMemory(outBlob.pbData, outBlob.cbData);
    LocalFree(outBlob.pbData);
    return result;
}

/*
 * Class:     fastkeychain_FastKeychain
 * Method:    nativeWriteCredential
 * Signature: (Ljava/lang/String;Ljava/lang/String;[B)Z
 */
JNIEXPORT jboolean JNICALL Java_fastkeychain_FastKeychain_nativeWriteCredential
  (JNIEnv *env, jclass cls, jstring jTarget, jstring jUser, jbyteArray jSecret) {
    if (!jTarget || !jSecret) return JNI_FALSE;

    const jchar *targetChars = env->GetStringChars(jTarget, NULL);
    const jchar *userChars = jUser ? env->GetStringChars(jUser, NULL) : NULL;

    jsize secretLen = env->GetArrayLength(jSecret);
    jbyte *secretBytes = env->GetByteArrayElements(jSecret, NULL);

    CREDENTIALW cred;
    ZeroMemory(&cred, sizeof(cred));
    cred.Type = CRED_TYPE_GENERIC;
    cred.TargetName = (LPWSTR)targetChars;
    cred.UserName = userChars ? (LPWSTR)userChars : (LPWSTR)L"FastJava";
    cred.CredentialBlobSize = (DWORD)secretLen;
    cred.CredentialBlob = (LPBYTE)secretBytes;
    cred.Persist = CRED_PERSIST_LOCAL_MACHINE; // Persists across reboots

    BOOL success = CredWriteW(&cred, 0);

    if (secretBytes) {
        SecureZeroMemory(secretBytes, secretLen);
        env->ReleaseByteArrayElements(jSecret, secretBytes, JNI_ABORT);
    }
    if (userChars) env->ReleaseStringChars(jUser, userChars);
    if (targetChars) env->ReleaseStringChars(jTarget, targetChars);

    return success ? JNI_TRUE : JNI_FALSE;
}

/*
 * Class:     fastkeychain_FastKeychain
 * Method:    nativeReadCredential
 * Signature: (Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_fastkeychain_FastKeychain_nativeReadCredential
  (JNIEnv *env, jclass cls, jstring jTarget) {
    if (!jTarget) return NULL;

    const jchar *targetChars = env->GetStringChars(jTarget, NULL);

    PCREDENTIALW pCred = NULL;
    BOOL success = CredReadW((LPCWSTR)targetChars, CRED_TYPE_GENERIC, 0, &pCred);

    env->ReleaseStringChars(jTarget, targetChars);

    if (!success || !pCred) {
        return NULL;
    }

    jbyteArray result = NULL;
    if (pCred->CredentialBlob && pCred->CredentialBlobSize > 0) {
        result = env->NewByteArray(pCred->CredentialBlobSize);
        if (result) {
            env->SetByteArrayRegion(result, 0, pCred->CredentialBlobSize, (jbyte*)pCred->CredentialBlob);
        }
    }

    // Secure wipe memory before free
    if (pCred->CredentialBlob) {
        SecureZeroMemory(pCred->CredentialBlob, pCred->CredentialBlobSize);
    }
    CredFree(pCred);

    return result;
}

/*
 * Class:     fastkeychain_FastKeychain
 * Method:    nativeDeleteCredential
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_fastkeychain_FastKeychain_nativeDeleteCredential
  (JNIEnv *env, jclass cls, jstring jTarget) {
    if (!jTarget) return JNI_FALSE;

    const jchar *targetChars = env->GetStringChars(jTarget, NULL);
    BOOL success = CredDeleteW((LPCWSTR)targetChars, CRED_TYPE_GENERIC, 0);
    env->ReleaseStringChars(jTarget, targetChars);

    return success ? JNI_TRUE : JNI_FALSE;
}

#ifdef __cplusplus
}
#endif
