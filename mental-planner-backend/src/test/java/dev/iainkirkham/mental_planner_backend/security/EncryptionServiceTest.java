package dev.iainkirkham.mental_planner_backend.security;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EncryptionServiceTest {

    private final EncryptionService encryptionService = new EncryptionService();

    @Test
    void encryptThenDecrypt_returnsOriginalPlaintext() {
        SecretKey key = encryptionService.generateKey();
        String plaintext = "Book therapy appointment for Thursday";

        String ciphertext = encryptionService.encrypt(plaintext, key);
        String decrypted = encryptionService.decrypt(ciphertext, key);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void encrypt_doesNotStorePlaintextInOutput() {
        SecretKey key = encryptionService.generateKey();
        String plaintext = "confidential mood journal entry";

        String ciphertext = encryptionService.encrypt(plaintext, key);

        assertThat(ciphertext).doesNotContain(plaintext);
    }

    @Test
    void encrypt_producesDifferentCiphertextForSamePlaintextEachCall() {
        SecretKey key = encryptionService.generateKey();
        String plaintext = "same note text";

        String first = encryptionService.encrypt(plaintext, key);
        String second = encryptionService.encrypt(plaintext, key);

        assertThat(first).isNotEqualTo(second);
        assertThat(encryptionService.decrypt(first, key)).isEqualTo(plaintext);
        assertThat(encryptionService.decrypt(second, key)).isEqualTo(plaintext);
    }

    @Test
    void decrypt_withWrongKey_throwsRatherThanReturningGarbageSilently() {
        SecretKey correctKey = encryptionService.generateKey();
        SecretKey wrongKey = encryptionService.generateKey();
        String ciphertext = encryptionService.encrypt("sensitive value", correctKey);

        assertThatThrownBy(() -> encryptionService.decrypt(ciphertext, wrongKey))
                .isInstanceOf(EncryptionException.class);
    }

    @Test
    void decrypt_withTamperedCiphertext_throws() {
        SecretKey key = encryptionService.generateKey();
        String ciphertext = encryptionService.encrypt("sensitive value", key);
        String encoded = ciphertext.substring(EncryptionService.CIPHERTEXT_PREFIX.length());

        byte[] raw = Base64.getDecoder().decode(encoded);
        raw[raw.length - 1] ^= (byte) 0xFF; // flip the last byte of the auth tag
        String tampered = EncryptionService.CIPHERTEXT_PREFIX + Base64.getEncoder().encodeToString(raw);

        assertThatThrownBy(() -> encryptionService.decrypt(tampered, key))
                .isInstanceOf(EncryptionException.class);
    }

    @Test
    void generateKey_returns256BitAesKey() {
        SecretKey key = encryptionService.generateKey();

        assertThat(key.getAlgorithm()).isEqualTo("AES");
        assertThat(key.getEncoded()).hasSize(32);
    }

    @Test
    void isEncrypted_distinguishesCiphertextFromLegacyPlaintextAndNull() {
        SecretKey key = encryptionService.generateKey();
        String ciphertext = encryptionService.encrypt("some value", key);

        assertThat(encryptionService.isEncrypted(ciphertext)).isTrue();
        assertThat(encryptionService.isEncrypted("plain legacy text")).isFalse();
        assertThat(encryptionService.isEncrypted(null)).isFalse();
    }

    @Test
    void decrypt_onLegacyPlaintextValue_throwsInsteadOfMisinterpretingIt() {
        SecretKey key = encryptionService.generateKey();

        assertThatThrownBy(() -> encryptionService.decrypt("legacy unencrypted title", key))
                .isInstanceOf(EncryptionException.class);
    }
}
