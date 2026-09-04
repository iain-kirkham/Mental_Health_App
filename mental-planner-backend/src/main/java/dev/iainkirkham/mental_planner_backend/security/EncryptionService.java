package dev.iainkirkham.mental_planner_backend.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256-GCM encrypt/decrypt primitive. Used both to wrap per-user data keys with the
 * application master key, and (once wired into entity converters) to encrypt field values
 * with a user's data key.
 * <p>
 * Output format is {@code "v1:" + base64(IV || ciphertext || authTag)}, with a fresh random
 * 96-bit IV generated on every call - callers must never reuse an IV with the same key. The
 * {@code v1:} prefix is a format marker, not a secret: it lets callers (e.g. the backfill
 * runner) tell already-encrypted values apart from legacy plaintext without attempting a
 * decrypt, and gives future algorithm/format changes a version to branch on.
 */
@Service
public class EncryptionService {

    /**
     * Marks a stored value as ciphertext produced by this class, distinguishing it from
     * legacy plaintext left over from before field encryption was introduced. See
     * {@link #isEncrypted(String)}.
     */
    public static final String CIPHERTEXT_PREFIX = "v1:";

    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int AES_256_KEY_LENGTH_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(byte[] plaintext, SecretKey key) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertextAndTag = cipher.doFinal(plaintext);

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertextAndTag.length);
            buffer.put(iv).put(ciphertextAndTag);
            return CIPHERTEXT_PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException e) {
            throw new EncryptionException("Failed to encrypt value", e);
        } finally {
            // Best-effort key hygiene: the caller's plaintext copy (e.g. a raw data key, or a
            // String's throwaway UTF-8 bytes) is never needed again after this call: overwrite
            // it rather than leaving it to linger on the heap until GC reclaims it. This does
            // NOT zero the original String, if the caller had one - Java Strings are immutable
            // and can't be wiped; see docs/security/data-at-rest-encryption-plan.md.
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    public String encrypt(String plaintext, SecretKey key) {
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8), key);
    }

    /**
     * True if {@code value} looks like ciphertext produced by {@link #encrypt}, false for
     * null or legacy plaintext. Used to skip already-encrypted rows during backfill without
     * needing to attempt (and catch a failed) decrypt.
     */
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(CIPHERTEXT_PREFIX);
    }

    public byte[] decryptToBytes(String encoded, SecretKey key) {
        if (!isEncrypted(encoded)) {
            throw new EncryptionException(
                    "Value is not in the expected \"" + CIPHERTEXT_PREFIX + "\"-prefixed ciphertext format", null);
        }
        byte[] decoded = Base64.getDecoder().decode(encoded.substring(CIPHERTEXT_PREFIX.length()));
        if (decoded.length < GCM_IV_LENGTH_BYTES) {
            throw new EncryptionException("Ciphertext too short to contain an IV", null);
        }
        byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH_BYTES);
        byte[] ciphertextAndTag = Arrays.copyOfRange(decoded, GCM_IV_LENGTH_BYTES, decoded.length);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return cipher.doFinal(ciphertextAndTag);
        } catch (GeneralSecurityException e) {
            // GCM's auth tag check fails here for wrong key, corrupted ciphertext, or
            // tampering - a real "who could read this" boundary, not just a format error.
            throw new EncryptionException(
                    "Failed to decrypt value - ciphertext may be corrupt, truncated, or encrypted under a different key", e);
        }
    }

    public String decrypt(String encoded, SecretKey key) {
        return new String(decryptToBytes(encoded, key), StandardCharsets.UTF_8);
    }

    /**
     * Generates a random AES-256 key, e.g. for a new per-user data encryption key.
     */
    public SecretKey generateKey() {
        byte[] keyBytes = new byte[AES_256_KEY_LENGTH_BYTES];
        secureRandom.nextBytes(keyBytes);
        try {
            // SecretKeySpec clones the array internally, so zeroing our copy afterward
            // doesn't affect the returned key.
            return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }

    public SecretKey toKey(byte[] rawKeyBytes) {
        return new SecretKeySpec(rawKeyBytes, KEY_ALGORITHM);
    }
}
