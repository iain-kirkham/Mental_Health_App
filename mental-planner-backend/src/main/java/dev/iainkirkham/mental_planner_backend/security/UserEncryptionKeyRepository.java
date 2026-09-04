package dev.iainkirkham.mental_planner_backend.security;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEncryptionKeyRepository extends JpaRepository<UserEncryptionKey, String> {
}
