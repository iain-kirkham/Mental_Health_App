package dev.iainkirkham.mental_planner_backend.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.iainkirkham.mental_planner_backend.config.AuthenticationContext;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.List;

/**
 * Encrypts a {@code List<String>} entity attribute as a single ciphertext (the list is
 * JSON-serialized, then encrypted as one value) rather than per-element, since the whole
 * list is one piece of user content. Used for {@code MoodEntry.factors}, which was
 * previously stored as a plain JSONB column - see docs/security/data-at-rest-encryption-plan.md.
 */
@Component
@Converter(autoApply = false)
public class EncryptedStringListConverter implements AttributeConverter<List<String>, String> {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final EncryptionService encryptionService;
    private final UserDataKeyService userDataKeyService;
    private final AuthenticationContext authenticationContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // See EncryptedStringConverter for why this dependency must be @Lazy.
    public EncryptedStringListConverter(EncryptionService encryptionService,
                                         @Lazy UserDataKeyService userDataKeyService,
                                         AuthenticationContext authenticationContext) {
        this.encryptionService = encryptionService;
        this.userDataKeyService = userDataKeyService;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) {
            return null;
        }
        return encryptionService.encrypt(writeJson(attribute), currentUserDataKey());
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return readJson(encryptionService.decrypt(dbData, currentUserDataKey()));
    }

    private SecretKey currentUserDataKey() {
        return userDataKeyService.getDataKey(authenticationContext.getCurrentUserId());
    }

    private String writeJson(List<String> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new EncryptionException("Failed to serialize value for encryption", e);
        }
    }

    private List<String> readJson(String json) {
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new EncryptionException("Failed to deserialize decrypted value", e);
        }
    }
}
