package dev.iainkirkham.mental_planner_backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * Loads the application master key (KEK) used to wrap/unwrap per-user data encryption keys.
 * The KEK is never persisted to the database - it is read once from an environment variable
 * (a Heroku Config Var in production, {@code APP_MASTER_KEY} locally) and held only in memory
 * for the life of the process. See docs/security/data-at-rest-encryption-plan.md.
 */
@Component
public class MasterKeyProvider {

    private static final int REQUIRED_KEY_LENGTH_BYTES = 32;

    private final SecretKey masterKey;

    public MasterKeyProvider(@Value("${app.encryption.master-key:}") String base64MasterKey) {
        this.masterKey = decode(base64MasterKey);
    }

    public SecretKey getMasterKey() {
        return masterKey;
    }

    private static SecretKey decode(String base64MasterKey) {
        if (base64MasterKey == null || base64MasterKey.isBlank()) {
            throw new IllegalStateException(
                    "APP_MASTER_KEY is not configured. Generate one with `openssl rand -base64 32` " +
                    "and set it as a Heroku config var / local env var - it must never be committed " +
                    "to source control or stored in the database.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64MasterKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("APP_MASTER_KEY is not valid base64", e);
        }
        if (keyBytes.length != REQUIRED_KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "APP_MASTER_KEY must decode to exactly " + REQUIRED_KEY_LENGTH_BYTES +
                    " bytes (a 256-bit AES key), got " + keyBytes.length);
        }
        try {
            // SecretKeySpec clones keyBytes internally, so zeroing our copy afterward doesn't
            // affect the returned key - see EncryptionService.generateKey().
            return new SecretKeySpec(keyBytes, "AES");
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }
}
