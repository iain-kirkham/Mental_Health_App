package dev.iainkirkham.mental_planner_backend.security;

import dev.iainkirkham.mental_planner_backend.config.AuthenticationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptedStringConverterTest {

    private static final String USER_ID = "user_123";

    @Mock
    private UserDataKeyService userDataKeyService;

    @Mock
    private AuthenticationContext authenticationContext;

    private final EncryptionService encryptionService = new EncryptionService();

    private EncryptedStringConverter converter;

    @BeforeEach
    void setUp() {
        converter = new EncryptedStringConverter(encryptionService, userDataKeyService, authenticationContext);
        lenient().when(authenticationContext.getCurrentUserId()).thenReturn(USER_ID);
    }

    @Test
    void convertToEntityAttribute_onCiphertext_decryptsIt() {
        SecretKey key = encryptionService.generateKey();
        when(userDataKeyService.getDataKey(USER_ID)).thenReturn(key);
        String ciphertext = encryptionService.encrypt("some note", key);

        assertThat(converter.convertToEntityAttribute(ciphertext)).isEqualTo("some note");
    }

    @Test
    void convertToEntityAttribute_onLegacyPlaintext_passesItThroughWithoutTouchingTheKey() {
        String result = converter.convertToEntityAttribute("a note written before encryption shipped");

        assertThat(result).isEqualTo("a note written before encryption shipped");
        verifyNoInteractions(userDataKeyService);
    }

    @Test
    void convertToEntityAttribute_onNull_returnsNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        verifyNoInteractions(userDataKeyService);
    }

    @Test
    void convertToDatabaseColumn_alwaysEncryptsRegardlessOfLegacyTolerance() {
        SecretKey key = encryptionService.generateKey();
        when(userDataKeyService.getDataKey(USER_ID)).thenReturn(key);

        String stored = converter.convertToDatabaseColumn("fresh note");

        assertThat(encryptionService.isEncrypted(stored)).isTrue();
        assertThat(encryptionService.decrypt(stored, key)).isEqualTo("fresh note");
    }
}
