package dev.iainkirkham.mental_planner_backend.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A user's data encryption key (DEK), wrapped (encrypted) under the application master key.
 * The unwrapped DEK never touches this table or any other row in the database - only the
 * ciphertext does. See docs/security/data-at-rest-encryption-plan.md.
 */
@Entity
@Table(name = "user_encryption_keys")
@Getter
@Setter
@NoArgsConstructor
public class UserEncryptionKey {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "wrapped_dek", nullable = false, columnDefinition = "TEXT")
    private String wrappedDek;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UserEncryptionKey(String userId, String wrappedDek) {
        this.userId = userId;
        this.wrappedDek = wrappedDek;
    }
}
