package dev.iainkirkham.mental_planner_backend.security;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Arrays;

/**
 * Gets or creates the per-user data encryption key (DEK) used to encrypt that user's
 * field values, transparently handling envelope wrapping/unwrapping under the application
 * master key and short-lived caching so callers never see key material management.
 */
@Service
public class UserDataKeyService {

    private final UserEncryptionKeyRepository repository;
    private final EncryptionService encryptionService;
    private final MasterKeyProvider masterKeyProvider;
    private final UserDataKeyCache keyCache;

    public UserDataKeyService(UserEncryptionKeyRepository repository,
                               EncryptionService encryptionService,
                               MasterKeyProvider masterKeyProvider,
                               UserDataKeyCache keyCache) {
        this.repository = repository;
        this.encryptionService = encryptionService;
        this.masterKeyProvider = masterKeyProvider;
        this.keyCache = keyCache;
    }

    /**
     * Returns the given user's data encryption key, generating and persisting one
     * (wrapped under the master key) on first use.
     */
    public SecretKey getDataKey(String userId) {
        SecretKey cached = keyCache.get(userId);
        if (cached != null) {
            return cached;
        }

        SecretKey dataKey = unwrap(findOrCreateWrappedKey(userId));
        keyCache.put(userId, dataKey);
        return dataKey;
    }

    private String findOrCreateWrappedKey(String userId) {
        return repository.findById(userId)
                .map(UserEncryptionKey::getWrappedDek)
                .orElseGet(() -> createWrappedKey(userId));
    }

    @Transactional
    protected String createWrappedKey(String userId) {
        SecretKey newDataKey = encryptionService.generateKey();
        String wrapped = encryptionService.encrypt(newDataKey.getEncoded(), masterKeyProvider.getMasterKey());

        try {
            repository.save(new UserEncryptionKey(userId, wrapped));
            return wrapped;
        } catch (DataIntegrityViolationException raceLost) {
            // Another concurrent request already created this user's key first - use theirs.
            return repository.findById(userId)
                    .map(UserEncryptionKey::getWrappedDek)
                    .orElseThrow(() -> raceLost);
        }
    }

    private SecretKey unwrap(String wrappedDek) {
        byte[] rawKey = encryptionService.decryptToBytes(wrappedDek, masterKeyProvider.getMasterKey());
        try {
            // toKey()/SecretKeySpec clones rawKey internally, so zeroing our copy afterward
            // doesn't affect the returned key - see EncryptionService.generateKey().
            return encryptionService.toKey(rawKey);
        } finally {
            Arrays.fill(rawKey, (byte) 0);
        }
    }
}
