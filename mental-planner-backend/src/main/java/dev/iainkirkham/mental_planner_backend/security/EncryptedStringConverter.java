package dev.iainkirkham.mental_planner_backend.security;

import dev.iainkirkham.mental_planner_backend.config.AuthenticationContext;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Transparently encrypts/decrypts a String entity attribute with the current user's data
 * key. Applied per-field via {@code @Convert} rather than autoApply, since not every String
 * column should be encrypted - see docs/security/data-at-rest-encryption-plan.md for which.
 * <p>
 * Relies on every read already being scoped to the authenticated user's own rows (as
 * enforced by the repository/service layer) - decrypting a row owned by someone else would
 * fail loudly with an {@link EncryptionException} rather than silently return garbage,
 * since AES-GCM's auth tag check fails under the wrong key.
 */
@Component
@Converter(autoApply = false)
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final EncryptionService encryptionService;
    private final UserDataKeyService userDataKeyService;
    private final AuthenticationContext authenticationContext;

    // UserDataKeyService is @Lazy because it depends (transitively) on a JPA repository,
    // which needs the EntityManagerFactory to exist - but Hibernate instantiates this
    // converter bean *while building* the EntityManagerFactory, so an eager dependency
    // here would be a circular bean-creation cycle. The proxy defers real resolution
    // until the first encrypt/decrypt call, by which point the EMF is fully built.
    public EncryptedStringConverter(EncryptionService encryptionService,
                                     @Lazy UserDataKeyService userDataKeyService,
                                     AuthenticationContext authenticationContext) {
        this.encryptionService = encryptionService;
        this.userDataKeyService = userDataKeyService;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return encryptionService.encrypt(attribute, currentUserDataKey());
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return encryptionService.decrypt(dbData, currentUserDataKey());
    }

    private SecretKey currentUserDataKey() {
        return userDataKeyService.getDataKey(authenticationContext.getCurrentUserId());
    }
}
