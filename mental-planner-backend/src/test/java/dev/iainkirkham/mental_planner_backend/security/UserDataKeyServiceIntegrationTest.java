package dev.iainkirkham.mental_planner_backend.security;

import dev.iainkirkham.mental_planner_backend.config.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link UserDataKeyService} caches the unwrapped data key in a request-scoped bean, so
 * these tests bind a mock request context per test to stand in for a real HTTP request.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class UserDataKeyServiceIntegrationTest {

    @BeforeEach
    void bindMockRequestContext() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void clearMockRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Autowired
    private UserDataKeyService userDataKeyService;

    @Autowired
    private UserEncryptionKeyRepository userEncryptionKeyRepository;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private MasterKeyProvider masterKeyProvider;

    @Test
    void getDataKey_forNewUser_generatesAndPersistsAWrappedKey() {
        String userId = "user_new_" + System.nanoTime();

        SecretKey dataKey = userDataKeyService.getDataKey(userId);

        assertThat(dataKey.getEncoded()).hasSize(32);
        assertThat(userEncryptionKeyRepository.findById(userId)).isPresent();
    }

    @Test
    void getDataKey_calledTwice_returnsTheSameKey() {
        String userId = "user_repeat_" + System.nanoTime();

        SecretKey first = userDataKeyService.getDataKey(userId);
        SecretKey second = userDataKeyService.getDataKey(userId);

        assertThat(second.getEncoded()).isEqualTo(first.getEncoded());
    }

    @Test
    void getDataKey_forDifferentUsers_returnsDifferentKeys() {
        SecretKey userAKey = userDataKeyService.getDataKey("user_a_" + System.nanoTime());
        SecretKey userBKey = userDataKeyService.getDataKey("user_b_" + System.nanoTime());

        assertThat(userAKey.getEncoded()).isNotEqualTo(userBKey.getEncoded());
    }

    @Test
    void persistedKey_isWrappedNotStoredInTheClear() {
        String userId = "user_wrapped_" + System.nanoTime();

        SecretKey dataKey = userDataKeyService.getDataKey(userId);
        String wrappedDek = userEncryptionKeyRepository.findById(userId).orElseThrow().getWrappedDek();

        // The stored ciphertext must not just be the raw key re-encoded.
        assertThat(wrappedDek).isNotEqualTo(Base64.getEncoder().encodeToString(dataKey.getEncoded()));

        // But unwrapping it with the master key recovers exactly the key that was handed back.
        byte[] unwrapped = encryptionService.decryptToBytes(wrappedDek, masterKeyProvider.getMasterKey());
        assertThat(unwrapped).isEqualTo(dataKey.getEncoded());
    }
}
