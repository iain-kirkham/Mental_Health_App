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
 * Output format is base64(IV || ciphertext || authTag), with a fresh random 96-bit IV
 * generated on every call - callers must never reuse an IV with the same key.
 */
@Service
public class EncryptionService {

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
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException e) {
            throw new EncryptionException("Failed to encrypt value", e);
        }
    }

    public String encrypt(String plaintext, SecretKey key) {
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8), key);
    }

    public byte[] decryptToBytes(String encoded, SecretKey key) {
        byte[] decoded = Base64.getDecoder().decode(encoded);
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
